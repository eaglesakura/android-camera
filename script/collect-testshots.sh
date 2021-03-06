#! /bin/bash -eu

rm -rf private/camera/junit

# run android tests
#adb shell am instrument -w -r -e debug false com.eaglesakura.android.camera.test/android.support.test.runner.AndroidJUnitRunner

# pull test shots
adb pull /sdcard/Android/data/com.eaglesakura.android.camera.test/files/shot/ private/camera/junit

for path in `find private/camera/junit -name "*.jpg"` ; do
    echo $path
    cp $path private/camera//images
done

adb shell rm -rf /sdcard/Android/data/com.eaglesakura.android.camera.test/files/shot/