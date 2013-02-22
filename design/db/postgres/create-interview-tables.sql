drop table interview_numeric_value;
drop table interview_categorical_value;
drop table interview;

CREATE TABLE
    calc.interview
    (
        interview_id SERIAL NOT NULL,
        obs_unit_id INTEGER NOT NULL,
--        plot_section_id INTEGER NULL,
        cluster_id INTEGER NULL,
        interview_no INTEGER NOT NULL,
        interview_date DATE NULL,
        interview_start_time TIME NULL,
        interview_end_time TIME NULL,
        interview_location Geometry(Point, 4326) NULL,
        interviewer_name VARCHAR(255) NULL,
        interviewee1_name VARCHAR(255) NULL,
        interviewee2_name VARCHAR(255) NULL,
        PRIMARY KEY (interview_id),
        CONSTRAINT interview_obs_unit_fkey FOREIGN KEY (obs_unit_id) REFERENCES observation_unit (obs_unit_id),
        CONSTRAINT interview_cluster_fkey FOREIGN KEY (cluster_id) REFERENCES cluster (cluster_id),        
        UNIQUE (obs_unit_id, cluster_id, interview_no)
    );

CREATE INDEX interview_location_gist
  ON calc.interview
  USING gist
  (interview_location);


CREATE TABLE
    interview_categorical_value
    (
        value_id SERIAL NOT NULL,
        interview_id INTEGER NOT NULL,
        category_id INTEGER NOT NULL,
        original BOOLEAN NOT NULL,
        current BOOLEAN NOT NULL,
        PRIMARY KEY (value_id),
        CONSTRAINT interview_categorical_value_category_fkey 
                FOREIGN KEY (category_id) 
                REFERENCES category (category_id),
        CONSTRAINT interview_categorical_value_interview_fkey 
                FOREIGN KEY (interview_id) 
                REFERENCES interview (interview_id)
    );
    
CREATE TABLE
    interview_numeric_value
    (
        value_id SERIAL NOT NULL,
        interview_id INTEGER NOT NULL,
        variable_id INTEGER NOT NULL,
        value DOUBLE PRECISION NOT NULL,
        original BOOLEAN NOT NULL,
        current BOOLEAN NOT NULL,
        CONSTRAINT interview_numeric_value_pkey PRIMARY KEY (value_id),
        CONSTRAINT interview_numeric_value_interview_fkey FOREIGN KEY (interview_id) REFERENCES interview (interview_id),
        CONSTRAINT interview_numeric_value_variable_fkey FOREIGN KEY (variable_id) REFERENCES variable (variable_id)
    );
