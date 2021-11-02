#!/bin/sh

cat build.gradle\
| awk '{$1=$1};1'\
| grep -i "implementation "\
| sed -e "s/(\"//g"\
| sed -e "s/\")//g"\
| sed -e "s/^implementation //Ig" -e "s/^testimplementation //Ig"\
| sed -e "s/\/\/.*//g"\
| sed -e "s/files(.*//g"\
| grep -v ^$\
| tr -d "'"\
| sed -e "s/\([-_[:alnum:]\.]*\):\([-_[:alnum:]\.]*\):\([-+_[:alnum:]\.]*\)/<dependency>\n\t<groupId>\1<\/groupId>\n\t<artifactId>\2<\/artifactId>\n\t<version>\3<\/version>\n<\/dependency>/g"
