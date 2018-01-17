cd C:\MyApps\Android\sdk\platform-tools
adb devices
adb -s emulator-5554 -e install –r android-server.apk
adb -s emulator-5554 shell am start -a android.intent.action.MAIN -n org.openqa.selenium.android.app/.MainActivity
adb forward tcp:8080 tcp:8080
