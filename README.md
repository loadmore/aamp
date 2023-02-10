# aamp
android aop 

更新为最新grade版本配置文件

 classpath 'com.android.tools.build:gradle:7.4.1'

 distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-all.zip

```
apply plugin: 'kotlin-kapt'

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'org.aspectj:aspectjtools:1.9.5'
    }
}


dependencies {
    implementation 'org.aspectj:aspectjrt:1.9.5'
    implementation project(':amp-runtime')
}


import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main

final def log = project.logger
final def variants = project.android.applicationVariants

variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return
    }

    TaskProvider<JavaCompile> taskProvider = variant.getJavaCompileProvider()

    taskProvider.configure {

        String path = javaCompile.options.bootstrapClasspath.join(File.pathSeparator)
        String cp = javaCompile.classpath.asFileTree.filter {
            !it.canonicalPath.contains("transforms")
        }.asPath

        javaCompile.doLast {
            String[] args = ["-showWeaveInfo",
                             "-1.9",
                             "-inpath", javaCompile.destinationDir.toString(),
                             "-aspectpath", cp,
                             "-d", javaCompile.destinationDir.toString(),
                             "-classpath", javaCompile.classpath.asPath,
                             "-bootclasspath", path]

            String[] kotlinArgs = ["-showWeaveInfo",
                                   "-1.9",
                                   "-inpath", project.buildDir.path + "/tmp/kotlin-classes/debug",
                                   "-aspectpath", cp,
                                   "-d", project.buildDir.path + "/tmp/kotlin-classes/debug",
                                   "-classpath", javaCompile.classpath.asPath,
                                   "-bootclasspath", path]


            MessageHandler handler = new MessageHandler(true)
            new Main().run(args, handler)
            new Main().run(kotlinArgs, handler)

            for (IMessage message : handler.getMessages(null, true)) {
                println("MethodTracing app log " + message.getKind().toString() + " " + message.message)
                switch (message.getKind()) {
                    case IMessage.ABORT:
                    case IMessage.ERROR:
                    case IMessage.FAIL:
                        log.error message.message, message.thrown
                        break
                    case IMessage.WARNING:
                        log.warn message.message, message.thrown
                        break
                    case IMessage.INFO:
                        log.info message.message, message.thrown
                        break
                    case IMessage.DEBUG:
                        log.debug message.message, message.thrown
                        break
                }
            }
        }
    }


}

```
