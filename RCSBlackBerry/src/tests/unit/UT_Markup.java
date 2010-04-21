package tests.unit;

import java.io.IOException;

import blackberry.log.Markup;
import blackberry.agent.Agent;
import blackberry.config.Keys;
import blackberry.utils.Utils;
import tests.AssertException;
import tests.TestUnit;
import tests.Tests;

public class UT_Markup extends TestUnit {

	public UT_Markup(String name, Tests tests) {
		super(name, tests);
	}

	void SimpleMarkupTest() throws AssertException {

		int agentId = Agent.AGENT_APPLICATION;
		Markup markup = new Markup(agentId, Keys.getInstance().getAesKey());


		if (markup.isMarkup())
			markup.removeMarkup();

		// senza markup
		boolean ret = markup.isMarkup();
		AssertThat(ret == false, "Should Not exist");

		// scrivo un markup vuoto
		ret = markup.writeMarkup( null);
		AssertThat(ret == true, "cannot write null markup");

		// verifico che ci sia
		ret = markup.isMarkup();
		AssertThat(ret == true, "Should exist");

		// scrivo un numero nel markup
		byte[] buffer = Utils.intToByteArray(123);

		ret = markup.writeMarkup( buffer);
		AssertThat(ret == true, "cannot write markup");

		// verifico che il numero si legga correttamente
		int value;
		try {
			byte[] read = markup.readMarkup();
			value = Utils.byteArrayToInt(read, 0);

		} catch (IOException e) {
			//#debug
debug.fatal("Markup read");
			throw new AssertException();
		}

		AssertEquals(value, 123, "Wrong read 123");

		// cancello il markup
		Markup.removeMarkup(agentId);

		// verifico che sia stato cancellato
		ret = markup.isMarkup();
		AssertThat(ret == false, "Should Not exist");

	}

	private void DeleteAllMarkupTest() {
		Markup.removeMarkups();
	}

	public boolean run() throws AssertException {

		DeleteAllMarkupTest();
		SimpleMarkupTest();

		return true;
	}

}
