CUSTOM_USER_DATA_PATH = "./custom_data"
IMAGE_PATH = "images/cse"
SUMMARIZE_CUSTOM_DATA = True
USE_GPU_FOR_INFERENCING = False
DEFINE_RARE_WORDS = False
LLM_FILTER_THRESHOLD = 6
RELEVANCE_FILTER_TIME = 120 # number of seconds the relevance filter looks back

DEBUG_FORCE_EXPERT_AGENT_RUN = False

GPT_4_MODEL = "gpt-4-1106-preview"
GPT_4_MAX_TOKENS = 2048
GPT_TEMPERATURE = 0.0

GPT_35_MODEL = "gpt-3.5-turbo-1106"
GPT_35_MAX_TOKENS = 2048

GPT_35_16K_MODEL = "gpt-35-turbo-16k"
GPT_35_16K_MAX_TOKENS = 4096

TIME_EVERYTHING = False

TESTING_LL_CONTEXT_CONVO_AGENT = True

### Features ###
PROACTIVE_AGENTS = 'proactive_agent_insights'
EXPLICIT_AGENT = 'explicit_agent_insights'
DEFINER_AGENT = 'intelligent_entity_definitions'
LANGUAGE_LEARNING_AGENT = 'language_learning'
LL_CONTEXT_CONVO_AGENT = 'll_context_convo'
ADHD_STMB_AGENT = 'adhd_stmb_agent_summaries'