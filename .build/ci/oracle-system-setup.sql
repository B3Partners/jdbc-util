ALTER SYSTEM SET PROCESSES=500 SCOPE = SPFILE;
ALTER SYSTEM SET NLS_LENGTH_SEMANTICS='CHAR' SCOPE = BOTH;
ALTER SYSTEM SET SPATIAL_VECTOR_ACCELERATION = TRUE;
SHUTDOWN IMMEDIATE;
