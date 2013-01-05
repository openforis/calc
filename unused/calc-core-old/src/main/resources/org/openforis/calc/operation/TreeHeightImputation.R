library(sqldf)
# load library lmfor
library(lmfor)

HDnaslund3 <- function (d, a, b, bh = 1.3) {
	d^2/(exp(a) + b * d)^2 + bh
}

startHDnaslund3 <- function (d, h, bh = 1.3) {
	start <- coef(lm(I(d/sqrt(h - bh)) ~ d))
	start[1]<-log(max(start[1],0.1))
	names(start) <- c("a", "b")
	start
}

data$D <- data$dbh
data$H <- data$totalh
data$clustID <- data$cluster_id
#data$clustID <- 1000 * data$clusx + data$clusy
#data$clustID <- data$id

data$D[data$D <= 0] <- NA
# SELECT ONLY LIVING TREES FOR ANALYSIS and trees where total h >= 1.35
# data$H[data$health.id == 7] <- -1
data$H[data$H < 1.35] <- NA

data$H2 <- data$H - 1.3

results <- fithd(data$D, data$H, data$clustID, model = "naslund", nranp = 2, random = NA,  varf = FALSE, na.omit = TRUE, start = NA, bh = 1.3)
results.cf <- coef(results)
results$clustID <- as.numeric(row.names(results.cf))

# get plots/clusters where Naslund A parameter is negative
row.names(results.cf)[results.cf[1]<0] -> checkThisData

if (length(checkThisData)>0) {
	results2<-fithd(data$D, data$H, data$clustID, model = "naslund3", nranp = 2, random = NA, varf = FALSE, na.omit = TRUE, start = NA, bh = 1.3)	
	results2.cf <- coef(results2)	
	for(i in 1:nrow(results.cf)){
		if (results.cf[i,1]<0) {
			results.cf[i,1]<-exp(results2.cf[i,1])
			results.cf[i,2]<-results2.cf[i,2]
		}
	}
	rm(results2)
	rm(checkThisData)
}

apu <- results.cf
apu$clustID <- as.numeric(row.names(results.cf))

uusidata <- sqldf("SELECT * FROM data JOIN apu USING(clustID)")
rm(apu)
uusidata$Hpred <- HDnaslund(uusidata$d, uusidata$a, uusidata$b, 1.3) 
uusidata$Hfinal[!is.na(uusidata$H)] <- uusidata$H[!is.na(uusidata$H)]
uusidata$Hfinal[is.na(uusidata$H)] <- uusidata$Hpred[is.na(uusidata$H)]

# MODEL PREDICTION BIAS AND RMSE:
harha <- sum(residuals(results))/length(residuals(results))
rmse <- sqrt(sum(residuals(results)^2)/length(residuals(results)))

#harha
#rmse

ImpFixed <- ImputeHeights(data$D, data$H, data$clustID, makeplot=FALSE, level=0)

data <- cbind(data, hpred=ImpFixed$h)
