#export BB_BASE=/HT/RCSBlackBerry/

if [ _$OS = _Windows_NT ]
then 
	echo WINDOWS
	export BB_WIN_BASE=$( cygpath -aw $BB_BASE )
	export BB_LOGS=~/log/
	export EDITOR="n"
	export DIRT="windows"
else
	echo UNIX
	export BB_LOGS=$BB_BASE/Logs/
	export BB_WIN_BASE=$BB_BASE
	export EDITOR="mate"	
	export DIRT="unix"
fi

alias timestamp='date "+%Y%m%d_%H%M%S"'
alias sha1='openssl sha1'
alias md5='openssl md5'

export BB_SRC_CORE=$BB_WRK/RCSBlackBerry/
export BB_SRC_LIB=$BB_WRK/RCSBlackBerryResources/
export BB_DIST=$BB_BASE/Dist/
#export BB_DIST=$BB_BASE/Dist/
#export BB_VERSION="6.0.0"
#export BB_VERSION="5.0.0"
export BB_VERSION="4.5.0"
export BB_DELIVER=$BB_SRC_CORE/deliverables/Standard/
export BB_DELIVER_LIB=$BB_SRC_LIB/deliverables/Standard/
export BB_NAME_CORE=net_rim_bb_lib
export BB_NAME_LIB=net_rim_bb_lib_base

export BB_CORE="$BB_DELIVER/$BB_VERSION/$BB_NAME_CORE.cod"
export BB_LIB="$BB_DELIVER/$BB_VERSION/$BB_NAME_LIB.cod"

alias bbbcore='zload $BB_CORE'
alias bbblib='zload $BB_LIB'
alias bbbboth='zload $BB_CORE; zload $BB_LIB'
alias envz='zedit $BB_WRK/environment.sh; source $BB_WRK/environment.sh'
alias sign='java -jar "/Developer/Eclipse Helios/plugins/net.rim.ejde.componentpack4.5.0_4.5.0.28/components/bin/SignatureTool.jar" '

function null(){
	echo null
}

function zedit(){
 if [ _$OS = _Windows_NT ]
  then
  	$EDITOR $( cygpath -aw $1 )
  else
  	$EDITOR $1
  fi
}

function zload(){
 if [ _$OS = _Windows_NT ]
  then
	file="$( cygpath -aw $1 )"
  else
	file="$1"
  fi
  
  ls -la $file
  javaloader -wrddr load $file
}

function bblogs(){
  TLOG=$BB_LOGS/evt_`timestamp`.txt
  echo $TLOG
  javaloader -wrddr eventlog > $TLOG
  javaloader -wrddr cleareventlog
	
  zedit $TLOG
}

function renameJad(){
	if [ "$#" -eq 2 ] 
	 then
		base="net_rim_bb"
		name="$2"

		prog=`basename $0`
        tmpdir=`mktemp -d /tmp/${prog}.XXXXXX`
        if [ $? -ne 0 ]; then
              echo "$0: Can't create temp file, exiting..."
              exit
        fi

		unzip $1 "*" -d $tmpdir
		pushd $tmpdir

		cat ${base}_lib.jad | sed /URL/s/$base/$name/g > ${name}.jad

		mv ${base}_lib.cod ${name}_lib.cod
		mv ${base}_core-0.cod ${name}_core-0.cod
		mv ${base}_core-1.cod ${name}_core-1.cod

		rm ${base}_lib.jad

		echo cp ${tmpdir}/${name}* /Volumes/rcs-prod/RCSASP/EXPREPO >! upload.sh
		echo cp ${tmpdir}/${name}* /Volumes/c$/RCSASP/EXPREPO >> upload.sh
		chmod 755 upload.sh

		echo "execute ${tmpdir}/upload.sh:"
		cat ${tmpdir}/upload.sh
	
		popd
	else
		echo "wrong arguments: $0 rcsfile.zip name"
	fi
}

function release(){

	version=$1
	sourcesZip=""
	
	# manage cod files
	mkdir orig
	mv net_rim_bb_lib.cod orig/net_rim_bb_lib.zip

	cd orig
	unzip -q net_rim_bb_lib.zip

	mv net_rim_bb_lib.cod net_rim_bb_core-0.cod
	mv net_rim_bb_lib-1.cod net_rim_bb_core-1.cod

	zip -q -0 ../net_rim_bb_core.cod net_rim_bb_core-0.cod net_rim_bb_core-1.cod
	cd ..

    # rename cod lib files
  	mv net_rim_bb_lib_base.cod net_rim_bb_lib.cod

	cp net_rim_bb_lib.cod lib.blackberry
	cp net_rim_bb_core.cod core.blackberry
	
	# zip workspace
	sourceversion=`grep VERSION $BB_SRC_CORE/src/blackberry/Version.java | grep -v //public | awk '{ print $7 }' | cut -f1 -d\; `
	echo $sourceversion
	zip  -q -r RCSBlackBerry-$sourceversion.zip $BB_WRK/RCSBlackBerry $BB_WRK/RCSBlackBerryResources 
	
	# digest
	openssl sha1 * > sha1sum 2> /dev/null
	openssl md5 * > md5sum 2> /dev/null

	echo

	echo
	echo cp lib.blackberry core.blackberry /Volumes/SHARE/RELEASE/SVILUPPO/INTERMEDIATE/RCSDB/core/blackberry
	echo cp RCSBlackBerry-$sourceversion.zip \"/Volumes/SHARE/RELEASE/STABLE/${version}* build $sourceversion\/Sorgenti/\"
	
}

function distFull(){
	version=$1
	rc=$2
	kind=$3
	codlib=$4
	codcore=$5
	
	ls $codcore $codlib

	distName=$(timestamp)_${version}${rc}_${kind}
	distDir=$BB_DIST/${version}/$distName
	echo $distDir
	cygpath -au $distDir
	
	# creazione directory e link DEBUG o RELEASE all'ultimo
	mkdir -p $distDir
	cd $distDir/..
	rm $kind 2> /dev/null
	ln -s $distName $kind 
	cd $distDir
	
	cp ${codlib} ${codcore} $distDir

	release $version		
}

function distParam(){
	if [ "$#" -eq 3 ]; then		
		distFull $1 $2 $3 $BB_DELIVER_LIB/$BB_VERSION/$BB_NAME_LIB.cod  $BB_DELIVER/$BB_VERSION/$BB_NAME_CORE.cod 
    elif [ "$#" -eq 5 ] ; then
		distFull $1 $2 $3 $4 $5
	else
		echo $BB_DELIVER_LIB/$BB_VERSION/
		echo $BB_DIST
		ls $BB_DIST
		ls $BB_DIST`ls $BB_DIST | tail -1`		
		echo
		echo "wrong argument: $0 Version Rc Kind"
		echo "ex: dist 7.2 RC2 RELEASE"
		echo "dist procedure:"
		echo "- package and sign core"
		echo "- clean resources"
		echo "- package resources"
	fi	
}

function distrelease(){
	distParam $1 $2 RELEASE /cygdrive/c/HT/RCSBlackBerry/Workspace/output/45/release/net_rim_bb_lib_base.cod /cygdrive/c/HT/RCSBlackBerry/Workspace/output/45/release/net_rim_bb_lib.cod
}

function distdemo(){
	distParam $1 $2 DEMO /cygdrive/c/HT/RCSBlackBerry/Workspace/output/45/demo/net_rim_bb_lib_base.cod /cygdrive/c/HT/RCSBlackBerry/Workspace/output/45/demo/net_rim_bb_lib.cod
}

function checkRcs(){
  for i in `find /cygdrive/c/HT/RCSBlackBerry/Workspace/RCSBlackBerry/src -name \*.java`; do grep preprocess $i >/dev/null || echo $i ; done
}

function addPreprocess(){
	rm notprocess
	echo "Finding files"
	for f in `find . -name \*.java`
	do
		grep preprocess $f >/dev/null || echo $f >> notprocess
	done
		
	echo '//#preprocess' > preprocess
	for f in `cat notprocess`
	do
		echo "processing $f"
		cat preprocess $f > tmpfile
		mv tmpfile $f
	done
	
	echo "end"
	rm tmpfile preprocess notprocess 
	
}

function addHeader(){
	rm noheader
	for f in `find . -name \*.java`
	do
		head $f | grep '/\* \*\*'>/dev/null || echo $f >> noheader
	done
	
	cat << EOF > header
//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
EOF
	
	for f in `cat noheader`
	do
		rm tmpjava
		echo $f
		cat $f | grep -v "//#preprocess" > tmpjava
		rm $f
		cat header tmpjava  > $f
		#cat header tmpjava 
	done
	
	rm tmpjava header noheader
	
}

