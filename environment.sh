#export BB_BASE=~/Projects/RCSBlackBerry/
export BB_LOGS=$BB_BASE/Logs/
export BB_WRK=$BB_BASE/Sources/Workspace/
export BB_SRC_CORE=$BB_WRK/RCSBlackBerry/
export BB_SRC_LIB=$BB_WRK/RCSBlackBerryResources/
export BB_DIST=$BB_BASE/dist/
export BB_VERSION="6.0.0"
export BB_VERSION="4.5.0"
export BB_DELIVER=$BB_SRC_CORE/deliverables/Standard/
export BB_DELIVER_LIB=$BB_SRC_LIB/deliverables/Standard/
export BB_NAME_CORE=net_rim_bb_lib
export BB_NAME_LIB=net_rim_bb_lib_base

alias bbbcore='javaloader -wrddr load $BB_DELIVER/$BB_VERSION/$BB_NAME_CORE.cod'
alias bbblib='javaloader -wrddr load $BB_DELIVER/$BB_VERSION/$BB_NAME_LIB.cod'
alias bbbboth='javaloader -wrddr load $BB_DELIVER/$BB_VERSION/$BB_NAME_LIB.cod $BB_DELIVER/$BB_VERSION/$BB_NAME_CORE.cod'
alias envz='vi $BB_WRK/environment.sh; source $BB_WRK/environment.sh'

function renameJad(){
	if [ "$#" -eq 1 ] 
	 then
		base="net_rim_bb"
		name="$1"

		cat ${base}_lib.jad | sed /URL/s/$base/$name/g > ${name}.jad

		mv ${base}_lib.cod ${name}_lib.cod
		mv ${base}_core-0.cod ${name}_core-0.cod
		mv ${base}_core-1.cod ${name}_core-1.cod

		rm ${base}_lib.jad

		echo cp ${name}* /Volumes/rcs-prod/RCSASP/EXPREPO
		echo cp ${name}* /Volumes/c$/RCSASP/EXPREPO
	else
		echo "wrong arguments: $0 name"
	fi
}


function bblogs(){
  TLOG=$BB_LOGS/evt_`timestamp`.txt
  echo $TLOG
  javaloader -wrddr eventlog > $TLOG
  javaloader -wrddr cleareventlog
  mate $TLOG
}

function release(){

	sourceversion=$1
	sourcesZip=""
	
	# manage cod files
	mkdir orig
	mv net_rim_bb_lib.cod orig/net_rim_bb_lib.zip

	cd orig
	unzip net_rim_bb_lib.zip

	mv net_rim_bb_lib.cod net_rim_bb_core-0.cod
	mv net_rim_bb_lib-1.cod net_rim_bb_core-1.cod

	zip -0 ../net_rim_bb_core.cod net_rim_bb_core-0.cod net_rim_bb_core-1.cod
	cd ..

    # rename cod lib files
  	mv net_rim_bb_lib_base.cod net_rim_bb_lib.cod

	cp net_rim_bb_lib.cod lib.blackberry
	cp net_rim_bb_core.cod core.blackberry
	
	# zip workspace
	zip -r RCSBlackBerry$sourceversion.zip $BB_WRK/RCSBlackBerry $BB_WRK/RCSBlackBerryResources
	
	# digest
	openssl sha1 * > sha1sum 2> /dev/null
	openssl md5 * > md5sum 2> /dev/null

	pwd
	echo cp lib.blackberry core.blackberry /Volumes/SHARE/RELEASE/SVILUPPO/INTERMEDIATE/RCSDB/core/files
	
}

function dist(){
	if [ "$#" -eq 3 ] 
	 then
		version=$1
		rc=$2
		kind=$3

		distDir=$BB_DIST/${version}/$(timestamp)_${version}${rc}_${kind}
		echo $distDir
		
		mkdir $distDir
		cd $distDir
		
		cp $BB_DELIVER_LIB/$BB_VERSION/$BB_NAME_LIB.cod $distDir
		cp $BB_DELIVER/$BB_VERSION/$BB_NAME_CORE.cod $distDir		
		
		#$(release)
		
	else
		echo "wrong argument: $0 Version Rc Kind"
	fi
	
}

function addPreprocess(){
	rm notprocess
	for f in `find . -name \*.java`
	do
		cat $f | grep preprocess >/dev/null || echo $f >>! notprocess
	done
	
	echo '//#preprocess\n' >! preprocess
	for f in `cat notprocess`
	do
		echo $f
		cat preprocess $f >! tmpfile
		mv tmpfile $f
	done
	
	rm tmpfile preprocess notprocess 
	
}

function addHeader(){
	rm noheader
	for f in `find . -name \*.java`
	do
		cat $f | grep ' * HT srl'>/dev/null || echo $f >>! noheader
	done
	
	cat << EOF > header
//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	
EOF
	
	for f in `cat noheader`
	do
		echo $f
		cat $f | grep -v "//#preprocess" >! tmpjava
		cat header tmpjava  >! $f
	done
	
	rm tmpjava header noheader
	
}

