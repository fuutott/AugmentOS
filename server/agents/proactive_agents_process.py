import time
import traceback
import math
import uuid
import asyncio
import logging

#custom
from DatabaseHandler import DatabaseHandler
from agents.proactive_meta_agent import run_proactive_meta_agent_and_experts
from server_config import openai_api_key
from logger_config import logger

time_between_iterations = 5
min_words_to_run = 8

def proactive_agents_processing_loop():
    print("START MULTI AGENT PROCESSING LOOP")
    dbHandler = DatabaseHandler(parent_handler=False)
    loop = asyncio.get_event_loop()

    while True:
        if not dbHandler.ready:
            print("dbHandler not ready")
            time.sleep(0.1)
            continue
        
        #wait for some transcripts to load in
        time.sleep(time_between_iterations)

        try:
            pLoopStartTime = time.time()
            # Check for new transcripts
            print("RUNNING MULTI-AGENT LOOP")
            newTranscripts = dbHandler.get_recent_transcripts_from_last_nseconds_for_all_users(n=time_between_iterations*2)
            for transcript in newTranscripts:
                if len(transcript['text'].split()) < min_words_to_run: # Around 75-100 words, no point to generate insight below this
                    if "?" not in transcript['text']: # Make an exception for questions
                        print("Transcript too short, skipping...")
                        continue
                # print("Run Insights generation with... user_id: '{}' ... text: '{}'".format(
                #     transcript['user_id'], transcript['text']))
                insightGenerationStartTime = time.time()
              
                # TODO: Test this quick n' dirty way of preventing proactive from running on explicit queries
                transcript_to_use = transcript['text']
                explicit_history = dbHandler.get_explicit_query_history_for_user(user_id=transcript['user_id'], device_id=None, should_consume=False, include_consumed=True)
                for hist_item in explicit_history:
                    transcript_to_use.replace(hist_item['query'], ' ... ')

                try:
                    insights_history = dbHandler.get_recent_nminutes_agent_insights_history_for_user(transcript['user_id'], n_minutes=90)
                    print("insights_history: {}".format(insights_history))
                    logger.log(level=logging.DEBUG, msg="Insights history: {}".format(insights_history))

                    #run proactive meta agent, get insights
                    insights = run_proactive_meta_agent_and_experts(transcript_to_use, insights_history)
                    print("insights: {}".format(insights))

                    for insight in insights:
                        if insight is None:
                            continue
                        #save this insight to the DB for the user
                        dbHandler.add_agent_insight_result_for_user(transcript['user_id'], insight["agent_name"], insight["agent_insight"], insight["reference_url"])

                except Exception as e:
                    print("Exception in agent.run()...:")
                    print(e)
                    traceback.print_exc()
                    continue
                insightGenerationEndTime = time.time()
                print("=== insightGeneration completed in {} seconds ===".format(
                    round(insightGenerationEndTime - insightGenerationStartTime, 2)))
        except Exception as e:
            print("Exception in Insight generator...:")
            print(e)
            traceback.print_exc()
