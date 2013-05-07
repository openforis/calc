-- Trees on inaccessible plots
SELECT distinct
    calc.specimen_view.cluster_code,
    calc.specimen_view.plot_no,
    calc.specimen_view.plot_section,
    calc.specimen_view.visit_type,
    count(plot_section.plot_section_id)
FROM
    calc.plot_section
INNER JOIN
        calc.specimen_view
ON
    (
        calc.specimen_view.plot_section_id = calc.plot_section.plot_section_id)
WHERE
    not(calc.plot_section.accessible)
group by 
    calc.specimen_view.cluster_code,
    calc.specimen_view.plot_no,
    calc.specimen_view.plot_section,
    calc.specimen_view.visit_type;