#proactively decides which agents to run and runs them
proactive_meta_agent_prompt_blueprint = """Convoscope is a tool that listens to a user's live conversation and enhances it by providing them with real time "Insights". The "Insights" generated should aim to lead the user to deeper understanding, broader perspectives, new ideas, more accurate information, better replies, etc..

You are given live transcripts from the user's conversation alongside information about a number of different 'expert agents` who have the power to generate "Insights". Your goal is to recognize when the output of an 'expert agent' would be useful to the conversation and to output a list of which agents should be run. It's OK to output an empty list if no agents should be run right now. It's OK to specify multiple agents

{force_run_agents_prompt}

[Your Agents]
You have access to "Expert Agents", which are like workers in your team that with special abilities. These are the agents you can run:
{expert_agents_descriptions_prompt}

[Conversation Transcript]
This is the current live transcript of the conversation you're assisting:
<transcript>{conversation_context}</transcript>

[Recent Insights History]
Here are the insights that have been generated recently, you should not call the expert agent if you think it will generate the same insight as one of these:
{insights_history}

<Task start>You should now output a list of the expert agents you think should run. Feel free to specify no expert agents. {format_instructions}<Task end>"""

#, but you should ussually just specify an empty list or only 1 agent.

#[Timing]
#The longer it's been without any insights generated, the more likely it is that an insight would be useful and welcome. It's good to have at least 1 insight every few minutes. So if the last insight time was 12 seconds ago, it's very unlikely that we need another insight right now. But if the last insight was 2 minutes ago, it's very likely we want a new insight. If the last insight was over 4 minutes ago and there's new transcripts, you should almost definitly specify an expert agent to create an insight, because it's been so long.

# , but they're expensive to run, so only specify agents that will be helpful

proactive_meta_agent_gatekeeper_prompt_blueprint = """[Objective]
Your job is to listen to a user's live conversation and determine if an "Insight" might be helpful to the conversation. 

"Insights" should lead the user to deeper understanding, broader perspectives, new ideas, more accurate information, better replies, and enhanced conversations.

[Types of "Insights"]
The following are "Expert Agents", which describe which types of "Insights" can be made:
{expert_agents_descriptions_prompt}

[Conversation Transcript]
This is the current live transcript of the conversation you're assisting:
<transcript>{conversation_context}</transcript>

<task>Output an "Insight helpfulness" score, which is a number 1 - 10, with 1 being not very helpful, and 10 being very useful/helpful.
If the score is 6 or higher, an "Insight" would be helpful to the conversation. If the score is 5 or lower, an "Insight" would not be helpful to the conversation.
{format_instructions}</task>"""
