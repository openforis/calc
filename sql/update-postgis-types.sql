ALTER TABLE "calc"."plot_survey" 
    ALTER COLUMN "gps_reading" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_survey" 
    ALTER COLUMN "location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_survey" 
    ALTER COLUMN "shape" TYPE geometry(Multipolygon,4326) USING NULL
GO
ALTER TABLE "calc"."plot" 
    ALTER COLUMN "location" TYPE geometry(Point,4326) USING NULL
GO
ALTER TABLE "calc"."plot_survey" 
    ALTER COLUMN "shape" TYPE geometry(Multipolygon,4326) USING NULL
GO
ALTER TABLE "calc"."aoi" 
    ALTER COLUMN "geometry" TYPE geometry(Multipolygon,4326) USING NULL
GO
