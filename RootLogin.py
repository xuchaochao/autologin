#coding:utf-8
import os
import sys
import subprocess
import sys

def rootlLogin(phone,password):

    oldLoginJava = r'/Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/src/Login/Login.java'
    with open(oldLoginJava, 'r',encoding='utf-8') as old:
        old_lines = old.readlines()
        new_lines = ''
        for line in old_lines:
            if line.strip()[0:22] == "mobileEditText.setText":
                new_lines ='\n' + new_lines + '\n' + 'mobileEditText.setText("{0}");'.format(phone) + '\n'
            elif line.strip()[0:24] == "passwordEditText.setText":
                new_lines = '\n' + new_lines + '\n' + 'passwordEditText.setText("{0}");'.format(password) + '\n'
            else:
                new_lines ='\n' + new_lines + line.strip() + '\n'
        java_new = new_lines.strip()
    with open("/Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/src/Login/Login.java","wt",encoding='utf-8') as f:
        f.write(java_new)

    oldBuildXml = r'/Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml'
    os.system("android create uitest-project -n {0} -t 2 -p /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin".format(phone))
    with open(oldBuildXml, 'r',encoding='utf-8') as old:
        old_lines = old.readlines()
        new_lines = ''
        for line in old_lines:
            if line.strip()[0:14] == "<project name=":
                new_lines ='\n' + new_lines + '\n' + '<project name="{0}" default="build">'.format(phone) + '\n'
            else:
                new_lines ='\n' + new_lines + line.strip() + '\n'
        xml_new = new_lines.strip()
    with open("/Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml","wt",encoding='utf-8') as f:
        f.write(xml_new)
    os.system("ant -buildfile /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml")
    os.system("adb push /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/bin/{0}.jar /data/local/tmp/".format(phone))
    os.system("adb shell uiautomator runtest {0}.jar -c Login.Login".format(phone))
    pass


if __name__ == '__main__':
    rootlLogin("18267990494","123456a")
    pass

# android create uitest-project -n 18267990494 -t 2 -p /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin
# open /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml
# ant -buildfile /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml
# adb -s O7E6HYS499999999 push /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/bin/18267990494.jar /data/local/tmp/
# adb -s O7E6HYS499999999 shell uiautomator runtest 18267990494.jar -c Login.Login

# <project name="15268509694" default="build">