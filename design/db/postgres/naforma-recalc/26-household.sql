SET search_path TO naforma1;

DROP TABLE IF EXISTS _household_agg;

CREATE TABLE _household_agg AS
SELECT
    hh.cluster_id,
    hh.sampling_unit,
    hh.distance_to_forest,
    hh.distance_to_su_centre,
    hh.time_study_start_time,
    hh.time_study_end_time,
    hh.household_member_count,
    hh.prev_household_member_count,
    hh.head_years_at_location,
    hh.head_birthplace,
    hh.house_wall_material,
    hh.wall_material_code_id,
    hh.house_wall_material_other,
    hh.house_roof_material,
    hh.roof_material_code_id,
    hh.house_roof_material_other,
    hh.alt_energy_sources_available,
    hh.private_property_change_type,
    hh.private_property_change_code_id,
    hh.private_property_change_amount,
    hh.private_property_change_amount_unit_name,
    hh.private_property_change_amount_unit,
    hh.private_property_change_reasons,
    hh.food_shortage_month_count,
    hh.supplemental_nwfp_used,
    hh.unexpected_expense_incurred,
    hh.forest_disturbances_occured,
    hh.can_reduce_consumption_flag,
    hh.can_reduce_consumption_comments,
    hh.can_protect_forests_better_flag,
    hh.can_protect_forests_better_comments,
    hh.rules_fair_flag,
    hh.rules_fair_comments,
    hh.penalties_fair_flag,
    hh.penalties_fair_comments,
    hh.group_participation_aware_of_pfm,
    hh.group_participation_aware_of_other,
    hh.group_participation_involvement,
    hh.group_participation_names,
    hh.group_participation_benefit_effects,
    hh.group_participation_benefit_code_id,
    hh.group_participation_patrol_frequency,
    hh.frequency_code_id,
    hh.group_participation_govt_visit_frequency,
    hh.group_participation_violators_caught,
    hh.violators_caught_code_id,
    hh.group_participation_punishment_decider,
    hh.punishment_decider_code_id,
    hh.group_participation_punishment_decider_other,
    hh.org_relationships_org_na,
    hh.forest_governance_accountability_problems_experienced,
    hh.forest_governance_accountability_assistance_requested,
    hh.forest_governance_accountability_assistance_response,
    hh.assistance_response_code_id,
    hh.forest_governance_accountability_assistance_response_other,
    hh.forest_governance_conflict_dispute_mgmt_conflict_experienced,
    hh.forest_governance_conflict_dispute_mgmt_resolution_attempted,
    hh.forest_governance_conflict_dispute_mgmt_forum_used,
    hh.forum_code_id,
    hh.forest_governance_conflict_dispute_mgmt_forum_effective,
    hh.forum_effective_code_id,
    hh.forest_governance_conflict_dispute_mgmt_forum_effective_other,
    hh.forest_governance_conflict_dispute_mgmt_conflict_ongoing,
    hh.forest_governance_monitoring_enforcement_first_contact,
    hh.first_contact_code_id,
    hh.forest_governance_monitoring_enforcement_reason,
    hh.first_contact_reason_code_id,
    hh.forest_governance_monitoring_enforcement_reason_other,
    hh.forest_governance_monitoring_enforcement_necessary,
    hh.forest_governance_transparency_denied_free_services,
    hh.forest_governance_transparency_requests,
    hh.forest_governance_equity_pfm_participation,
    hh.forest_governance_equity_pfm_benefits,
    hh.forest_governance_equity_fair_share,
    hh.share_code_code_id,
    hh.forest_governance_govt_assistance_assistance_received,
    hh.forest_governance_govt_assistance_total_days,
    hh.comments,
    hh.location,
    hh.district_id,
    hh.region_id,
    hh.zone_id,
    hh.country_id,
    hh.village,
    hh.ward,
    c.aoi_label                                            AS country,
    z.aoi_label                                            AS zone,
    r.aoi_label                                            AS region,
    d.aoi_label                                            AS district,
    head.interviewed                                       AS head_interviewed,
    head.age                                               AS head_age,
    naforma1.gender_code.gender_label_en                   AS head_gender,
    naforma1.education_level_code.education_level_label_en AS head_education,
    1 AS cnt
FROM
    naforma1._household hh
INNER JOIN
    calc.aoi d
ON
    (
        hh.district_id = d.aoi_id)
INNER JOIN
    calc.aoi r
ON
    (
        hh.region_id = r.aoi_id)
INNER JOIN
    calc.aoi z
ON
    (
        hh.zone_id = z.aoi_id)
INNER JOIN
    calc.aoi c
ON
    (
        hh.country_id = c.aoi_id)
INNER JOIN
    naforma1.head head
ON
    (
        hh.household_id = head.household_id AND head.position='head')
left outer join
    naforma1.gender_code
ON
    (
        head.gender_code_id = naforma1.gender_code.gender_code_id)
left outer join
    naforma1.education_level_code
ON
    (
        head.education_level_code_id = naforma1.education_level_code.education_level_code_id) ;        