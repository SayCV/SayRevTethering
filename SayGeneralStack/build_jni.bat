cd /d %~dp0

set PATH=%PATH%;D:/cygwin/bin
set HOME=%cd%
set NDK=D:/Android/android-ndk-r9b

:::bash.exe --login -c "cd $(cygpath -u 'd:\Android\projects\hello-jni') && pwd && $NDK/ndk-build"
rem bash.exe --login -c "cd jni && pwd && $NDK/ndk-build"

cd ../RevTethering
if not exist libs (
	mkdir libs
	cd libs
	if not exist armeabi (
 		mkdir armeabi
	)
	cd ..
)
cd ..

cp SayGeneralStack/libs/armeabi/libutils_armv5te.so RevTethering/libs/armeabi/
cp ../imsdroid/native-debug/libs/armeabi/libtinyWRAP.so RevTethering/libs/armeabi/libtinyWRAP_armv5te.so

pause
