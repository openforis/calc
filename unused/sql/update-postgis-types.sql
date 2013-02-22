ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "plot_gps_reading" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "plot_location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "plot_section_shape" TYPE geometry(Polygon,4326) USING NULL
GO
ALTER TABLE "calc"."sample_plot" 
    ALTER COLUMN "sample_plot_location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."sample_plot" 
    ALTER COLUMN "sample_plot_shape" TYPE geometry(Polygon,4326) USING NULL
GO
ALTER TABLE "calc"."aoi" 
    ALTER COLUMN "aoi_shape" TYPE geometry(Multipolygon,4326) USING NULL
GO
