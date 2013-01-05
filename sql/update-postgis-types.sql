ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "gps_reading" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_section" 
    ALTER COLUMN "shape" TYPE geometry(Polygon,4326) USING NULL
GO
ALTER TABLE "calc"."sample_plot" 
    ALTER COLUMN "location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."sample_plot" 
    ALTER COLUMN "shape" TYPE geometry(Polygon,4326) USING NULL
GO
ALTER TABLE "calc"."aoi" 
    ALTER COLUMN "shape" TYPE geometry(Multipolygon,4326) USING NULL
GO
