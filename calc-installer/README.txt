define in your settings.xml an active profile including the installbuilder-home property
e,.g.: 

	<profiles>
		<profile>
			<id>calc</id>
			<properties>
				<installbuilder-home>/opt/installbuilder-9.0.1</installbuilder-home>
			</properties>
		</profile>
	</profiles>
	
	<activeProfiles>
		<activeProfile>calc</activeProfile>
	</activeProfiles>