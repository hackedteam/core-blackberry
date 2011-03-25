package com.rim.samples.device.bbminjectdemo;

import java.util.Vector;

import net.rim.device.api.util.StringUtilities;

/**
 * Rappresenta una conversazione tra un certo numero di utenti.
 * Tiene traccia delle ultime linee.
 * 
 * Quando arriva un clip:
 *
 * clip coincide con last: niente da fare.
 * clip e' disgiunto da last: si cancella last e lo sostituisce con clip e si logga.
 * clip comincia in mezzo a last: si identifica dove comincia, si cancella il precedente,
 * si logga quello che c'e' di nuovo e si aggiorna lastlines
 * 
 * fintanto che le righe del clip hanno archivedPos > 0 si ignorano.
 * Quando ne compare una nuova si comincia a loggare.
 * 
 * @author zeno
 *
 */
public class Conversation {
	User[] partecipants;
	String subject;
	String program;

	Vector lastLines = new Vector();
	
	int archivedPos(Line line){
		return lastLines.indexOf(line);
	}
	
	void cleanLines(int pos){
		for (int i = pos; i < lastLines.size(); i++) {
			lastLines.removeElementAt(i);
		}
	}
	
	boolean addLine(Line line){
		
		if(!lastLines.contains(line)){
			log(line);
			lastLines.addElement(line);
			return true;
		}
		return false;
	}

	private void log(Line line) {
		
		
	}
}
