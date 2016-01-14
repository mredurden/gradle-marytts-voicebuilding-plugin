package de.dfki.mary.voicebuilding

import de.dfki.mary.voicebuilding.tasks.*

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

import org.apache.commons.codec.digest.DigestUtils

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class VoicebuildingLegacyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.plugins.apply VoicebuildingDataPlugin

        project.repositories {
            jcenter()
            maven {
                url 'https://oss.jfrog.org/artifactory/repo'
            }
        }

        project.configurations.create 'legacy'

        project.ext {
            legacyBuildDir = "$project.buildDir/mary"

            // configure speech-tools
            def proc = 'which ch_track'.execute()
            proc.waitFor()
            speechToolsDir = new File(proc.in.text)?.parentFile?.parent

            // configure praat
            proc = 'which praat'.execute()
            proc.waitFor()
            praat = proc.in.text
        }

        project.task('templates', type: LegacyTemplateTask) {
            destDir = project.file("$project.buildDir/templates")
        }

        project.task('legacyInit', type: LegacyInitTask) {
            dependsOn project.templates
        }

        project.task('legacyPraatPitchmarker', type: LegacyVoiceImportTask) {
            srcDir = project.file("$project.buildDir/wav")
            destDir = project.file("$project.buildDir/pm")
        }

        project.task('legacyMCEPMaker', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPraatPitchmarker
            srcDir = project.file("$project.buildDir/pm")
            destDir = project.file("$project.buildDir/mcep")
        }

        project.task('legacyPhoneUnitLabelComputer', type: LegacyVoiceImportTask) {
            srcDir = project.file("$project.buildDir/lab")
            destDir = project.file("$project.buildDir/phonelab")
        }

        project.task('legacyHalfPhoneUnitLabelComputer', type: LegacyVoiceImportTask) {
            srcDir = project.file("$project.buildDir/lab")
            destDir = project.file("$project.buildDir/halfphonelab")
        }

        project.task('legacyTranscriptionAligner', type: LegacyVoiceImportTask) {
            dependsOn project.generateAllophones
            srcDir = project.file("$project.buildDir/lab")
            destDir = project.file("$project.buildDir/allophones")
        }

        project.task('legacyFeatureLister', type: LegacyFeatureListerTask) {
            destFile = project.file("$project.legacyBuildDir/features.txt")
        }

        project.task('legacyPhoneUnitFeatureComputer', type: LegacyUnitFeatureComputerTask) {
            dependsOn project.legacyTranscriptionAligner, project.legacyFeatureLister
            rootFeature = 'phone'
            exclude = ['halfphone_lr', 'halfphone_unitname']
            outputType = 'TARGETFEATURES'
            srcDir = project.file("$project.buildDir/allophones")
            destDir = project.file("$project.buildDir/phonefeatures")
            fileExt = 'pfeats'
        }

        project.task('legacyHalfPhoneUnitFeatureComputer', type: LegacyUnitFeatureComputerTask) {
            dependsOn project.legacyTranscriptionAligner, project.legacyFeatureLister
            rootFeature = 'halfphone_unitname'
            exclude = []
            outputType = 'HALFPHONE_TARGETFEATURES'
            srcDir = project.file("$project.buildDir/allophones")
            destDir = project.file("$project.buildDir/halfphonefeatures")
            fileExt = 'hpfeats'
        }

        project.task('legacyWaveTimelineMaker', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPraatPitchmarker
            srcDir = project.file("$project.buildDir/wav")
            srcDir2 = project.file("$project.buildDir/pm")
            destFile = project.file("$project.legacyBuildDir/timeline_waveforms.mry")
        }

        project.task('legacyBasenameTimelineMaker', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPraatPitchmarker
            srcDir = project.file("$project.buildDir/wav")
            srcDir2 = project.file("$project.buildDir/pm")
            destFile = project.file("$project.legacyBuildDir/timeline_basenames.mry")
        }

        project.task('legacyMCepTimelineMaker', type: LegacyVoiceImportTask) {
            dependsOn project.legacyMCEPMaker
            srcDir = project.file("$project.buildDir/wav")
            srcDir2 = project.file("$project.buildDir/mcep")
            destFile = project.file("$project.legacyBuildDir/timeline_mcep.mry")
        }

        project.task('legacyPhoneLabelFeatureAligner', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPhoneUnitLabelComputer, project.legacyPhoneUnitFeatureComputer
            srcDir = project.file("$project.buildDir/phonelab")
        }

        project.task('legacyHalfPhoneLabelFeatureAligner', type: LegacyVoiceImportTask) {
            dependsOn project.legacyHalfPhoneUnitLabelComputer, project.legacyHalfPhoneUnitFeatureComputer
            srcDir = project.file("$project.buildDir/halfphonelab")
        }

        project.task('legacyPhoneUnitfileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPraatPitchmarker, project.legacyPhoneUnitLabelComputer
            dependsOn project.legacyPhoneLabelFeatureAligner
            srcDir = project.file("$project.buildDir/pm")
            destFile = project.file("$project.legacyBuildDir/phoneUnits.mry")
        }

        project.task('legacyHalfPhoneUnitfileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPraatPitchmarker, project.legacyHalfPhoneUnitLabelComputer
            dependsOn project.legacyHalfPhoneLabelFeatureAligner
            srcDir = project.file("$project.buildDir/pm")
            destFile = project.file("$project.legacyBuildDir/halfphoneUnits.mry")
        }

        project.task('legacyPhoneFeatureFileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPhoneUnitfileWriter, project.legacyPhoneUnitFeatureComputer
            srcFile = project.file("$project.legacyBuildDir/phoneUnits.mry")
            srcDir = project.file("$project.buildDir/phonefeatures")
            destFile = project.file("$project.legacyBuildDir/phoneFeatures.mry")
            destFile2 = project.file("$project.legacyBuildDir/phoneUnitFeatureDefinition.txt")
        }

        project.task('legacyHalfPhoneFeatureFileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyHalfPhoneUnitfileWriter
            dependsOn project.legacyHalfPhoneUnitFeatureComputer
            srcFile = project.file("$project.legacyBuildDir/halfphoneUnits.mry")
            srcDir = project.file("$project.buildDir/halfphonefeatures")
            destFile = project.file("$project.legacyBuildDir/halfphoneFeatures.mry")
            destFile2 = project.file("$project.legacyBuildDir/halfphoneUnitFeatureDefinition.txt")
        }

        project.task('legacyF0PolynomialFeatureFileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyHalfPhoneUnitfileWriter
            dependsOn project.legacyWaveTimelineMaker
            dependsOn project.legacyHalfPhoneFeatureFileWriter
            srcFile = project.file("$project.legacyBuildDir/halfphoneUnits.mry")
            srcFile2 = project.file("$project.legacyBuildDir/timeline_waveforms.mry")
            srcFile3 = project.file("$project.legacyBuildDir/halfphoneFeatures.mry")
            destFile project.file("$project.legacyBuildDir/syllableF0Polynomials.mry")
        }

        project.task('legacyAcousticFeatureFileWriter', type: LegacyVoiceImportTask) {
            dependsOn project.legacyHalfPhoneUnitfileWriter
            dependsOn project.legacyF0PolynomialFeatureFileWriter
            dependsOn project.legacyHalfPhoneFeatureFileWriter
            srcFile = project.file("$project.legacyBuildDir/halfphoneUnits.mry")
            srcFile2 = project.file("$project.legacyBuildDir/syllableF0Polynomials.mry")
            srcFile3 = project.file("$project.legacyBuildDir/halfphoneFeatures.mry")
            destFile = project.file("$project.legacyBuildDir/halfphoneFeatures_ac.mry")
            destFile2 = project.file("$project.legacyBuildDir/halfphoneUnitFeatureDefinition_ac.txt")
        }

        project.task('legacyJoinCostFileMaker', type: LegacyVoiceImportTask) {
            dependsOn project.legacyMCepTimelineMaker
            dependsOn project.legacyHalfPhoneUnitfileWriter
            dependsOn project.legacyAcousticFeatureFileWriter
            srcFile = project.file("$project.legacyBuildDir/timeline_mcep.mry")
            srcFile2 = project.file("$project.legacyBuildDir/halfphoneUnits.mry")
            srcFile3 = project.file("$project.legacyBuildDir/halfphoneFeatures_ac.mry")
            destFile = project.file("$project.legacyBuildDir/joinCostFeatures.mry")
            destFile2 = project.file("$project.legacyBuildDir/joinCostWeights.txt")
        }

        project.task('legacyCARTBuilder', type: LegacyVoiceImportTask) {
            dependsOn project.legacyAcousticFeatureFileWriter
            srcFile = project.file("$project.legacyBuildDir/halfphoneFeatures_ac.mry")
            destFile = project.file("$project.legacyBuildDir/cart.mry")
        }

        project.task('legacyDurationCARTTrainer', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPhoneFeatureFileWriter
            dependsOn project.legacyPhoneUnitfileWriter
            dependsOn project.legacyWaveTimelineMaker
            srcFile = project.file("$project.legacyBuildDir/phoneFeatures.mry")
            srcFile2 = project.file("$project.legacyBuildDir/phoneUnits.mry")
            srcFile3 = project.file("$project.legacyBuildDir/timeline_waveforms.mry")
            destFile = project.file("$project.legacyBuildDir/dur.tree")
        }

        project.task('legacyF0CARTTrainer', type: LegacyVoiceImportTask) {
            dependsOn project.legacyPhoneFeatureFileWriter
            dependsOn project.legacyPhoneUnitfileWriter
            dependsOn project.legacyWaveTimelineMaker
            srcFile = project.file("$project.legacyBuildDir/phoneFeatures.mry")
            srcFile2 = project.file("$project.legacyBuildDir/phoneUnits.mry")
            srcFile3 = project.file("$project.legacyBuildDir/timeline_waveforms.mry")
            destFile = project.file("$project.legacyBuildDir/f0.left.tree")
            destFile2 = project.file("$project.legacyBuildDir/f0.mid.tree")
            destFile3 = project.file("$project.legacyBuildDir/f0.right.tree")
        }

        project.task('processLegacyResources', type: Copy) {
            from project.legacyAcousticFeatureFileWriter, {
                include 'halfphoneUnitFeatureDefinition_ac.txt'
            }
            from project.legacyJoinCostFileMaker, {
                include 'joinCostWeights.txt'
            }
            from project.legacyCARTBuilder
            into project.sourceSets.main.output.resourcesDir
            project.jar.dependsOn it
        }

        project.task('legacyZip', type: Zip) {
            dependsOn project.legacyBasenameTimelineMaker,
                    project.legacyDurationCARTTrainer,
                    project.legacyF0CARTTrainer,
                    project.legacyHalfPhoneFeatureFileWriter,
                    project.legacyJoinCostFileMaker,
                    project.legacyPhoneFeatureFileWriter,
                    project.legacyWaveTimelineMaker
            from project.jar, {
                rename { "lib/$it" }
            }
        }

        project.task('legacyDescriptor') {
            dependsOn project.legacyZip
            def zipFile = project.legacyZip.outputs.files.singleFile
            def xmlFile = project.file("$project.distsDir/$project.name-$project.version-component-descriptor.xml")
            inputs.files zipFile
            outputs.files xmlFile
            doLast {
                def zipFileHash = DigestUtils.md5Hex(new FileInputStream(zipFile))
                def builder = new StreamingMarkupBuilder()
                def xml = builder.bind {
                    'marytts-install'(xmlns: 'http://mary.dfki.de/installer') {
                        voice(gender: project.voice.gender, locale: project.voice.maryLocale, name: project.voice.name, type: project.voice.type, version: project.maryttsVersion) {
                            delegate.description project.voice.description
                            license(href: project.voice.license.url)
                            'package'(filename: zipFile.name, md5sum: zipFileHash, size: zipFile.size()) {
                                location(folder: true, href: "http://mary.dfki.de/download/$project.maryttsVersion/")
                            }
                            depends(language: project.voice.maryLocaleXml, version: project.maryttsVersion)
                        }
                    }
                }
                xmlFile.text = XmlUtil.serialize(xml)
            }
        }

        project.afterEvaluate {
            project.dependencies {
                compile "de.dfki.mary:marytts-lang-$project.voice.language:$project.maryttsVersion"
                legacy("de.dfki.mary:marytts-builder:$project.maryttsVersion") {
                    exclude module: 'mwdumper'
                    exclude module: 'sgt'
                }
                testCompile "junit:junit:4.11"
            }

            project.processLegacyResources {
                rename { "marytts/voice/$project.voice.nameCamelCase/$it" }
            }

            project.legacyZip {
                from project.legacyBuildDir, {
                    include 'dur.tree',
                            'f0.left.tree',
                            'f0.mid.tree',
                            'f0.right.tree',
                            'halfphoneFeatures_ac.mry',
                            'halfphoneUnits.mry',
                            'joinCostFeatures.mry',
                            'phoneUnitFeatureDefinition.txt',
                            'timeline_basenames.mry',
                            'timeline_waveforms.mry'
                    rename { "lib/voices/$project.voice.name/$it" }
                }
            }
        }
    }
}
