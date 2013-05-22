SET search_path TO naforma1_se;

ALTER TABLE _household
ADD COLUMN forest_change VARCHAR(255);

WITH ch AS (
    SELECT
        hh.household_id,
        luc.change
    FROM
        naforma1_se._household hh
    INNER JOIN
        naforma1_se.land_use_change luc
    ON
        hh.household_id = luc.household_id
    WHERE
        luc.type = '1'
)        
UPDATE
    _household 
SET
    forest_change = ch.change
FROM ch
WHERE ch.household_id = _household.household_id;    