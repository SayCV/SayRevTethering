cd /d %~dp0

set PATH=%PATH%;D:/cygwin/bin
set HOME=%cd%
set NDK=D:/Android/android-ndk-r9b

:::bash.exe --login -c "cd $(cygpath -u 'd:\Android\projects\hello-jni') && pwd && $NDK/ndk-build"
bash.exe --login -c "cd jni && pwd && $NDK/ndk-build"

rem cp libs/armeabi/saysu res/raw/saysu
pause
