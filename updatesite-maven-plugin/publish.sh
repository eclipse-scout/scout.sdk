#!/bin/sh
####
# publish build results on download area.
# /home/data/httpd/download.eclipse.org/scout
####
workingDir=/home/data/httpd/download.eclipse.org/scout
stagingArea=$workingDir/stagingArea
repositoriesDir=$workingDir
stageTriggerFileName=doStage

processZipFile() {
  backupDir=$(pwd)
  zipFile=$backupDir"/"${1%?}
  sigOk=$2
  if [ $sigOk == OK ]; then
    echo $(date)" publish $zipFile"
    mkdir $stagingArea/working
    unzip $zipFile -d $stagingArea/working >$stagingArea/NUL
    chgrp -R technology.scout $stagingArea/working
    chmod -R g+w $stagingArea/working

    cd $stagingArea/working
    for d in {[0-9\.]*,nightly,releases}; do
      if [ -d "$d" ]; then
        if [ -d $repositoriesDir/$d""_new ]; then
          rm -rf $repositoriesDir/$d""_new
        fi
        mv $stagingArea/working/$d $repositoriesDir/$d""_new
        # backup original
        # if [ -d $repositoriesDir/$d ]; then
        # if [ -d $repositoriesDir/$d""_backup ]; then
        # rm -rf $repositoriesDir/$d""_backup
        # fi
        # cp -r $repositoriesDir/$d $repositoriesDir/$d""_backup
        # fi
        # copy new repository
        cp -rf $repositoriesDir/$d""_new/* $repositoriesDir/$d
        rm -rf $repositoriesDir/$d""_new
      fi
    done

    truncateNightly $workingDir/nightly

    #cleanup stagingArea
    cp $stagingArea/working/*.xml $repositoriesDir/
    rm -rf $stagingArea/working
    cd $backupDir
  else
    echo "md5 not valid for $zipFile!"
  fi
}

## remove old nightly repositories that are not contained in the composite update site
truncateNightly() {
  cur=$(pwd)
  dir=$1
  echo "Current dir: ${cur}."
  echo "Recursively truncate dir: ${dir}."
  cd ${dir}
  for d in *; do
    if [ -d "$d" ]; then
      echo -e "\nTruncating Scout ${d} composite dir"
      truncateScoutVersion "$d"
    fi
  done
  cd ${cur}
}

## removes sub folders starting with scout of a specific Scout version, e.g. scout.main, scout.rap
truncateScoutVersion() {
  curdir=$1
  pushd ${curdir} >/dev/null
  for dir in scout*; do
    if [ -d "$dir" ]; then
      echo -e "\t\tTruncating composite dir ${dir}"
      truncateComposite "$dir"
    fi
  done
  popd >/dev/null
  # special handling for Scout 3.8 since folder structure is different.
  if echo ${curdir} | grep -w -q "3.8"; then
    echo -e "\t\tTruncating Scout ${curdir} composite dir"
    truncateComposite "$curdir"
  fi
}

## removes all folders starting with N that are not contained in the compositeContent.jar
truncateComposite() {
  cur=$(pwd)
  compositeDir=$1

  cd ${compositeDir}
  unzip -q -o compositeContent.jar
  for sub in N*; do
    echo -e "\t\t\tChecking nightly dir ${sub}"
    if [ -d "$sub" ]; then
      if ! (grep -q "$sub" compositeContent.xml); then
        echo -e "\t\t\t\t$sub is not contained in composite. Removing..."
        rm -rf $sub
      fi
    fi
  done
  rm compositeContent.xml
  cd ${cur}
}

backupDir=$(pwd)
cd $stagingArea
for f in doStage*; do
  if [ -f "$f" ]; then
    echo "Processing $f "
    mv $f processing
    processZipFile $(md5sum -c $stagingArea/processing)
  fi
done
rm -rf $stagingArea/*
chgrp -R technology.scout $workingDir
chmod g+w -R $workingDir
cd $backupDir

#echo $stagingArea/stage.zip
#username=$(ls -l $stagingArea/stage.zip | awk '{print $3}')
#if [ "$username" == "aho" ]; then
