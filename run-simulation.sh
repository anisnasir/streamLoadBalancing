#!/bin/bash
JAR="target/StreamLoadBalancing-0.0.5-SNAPSHOT-jar-with-dependencies.jar"
#indir="/var/root/Yahoo/input/twitter-no2spaces"
indir="skew_input/zipf_10000000_1000000_1.2.txt"
#indir="skew_input/wiki"
NumberOfServers="5 10 20 30 40 50 100"
NumberOfSources="5"
NumberOfChoices="2"
#ProbingMinutes="1 5 10 30"
lbname="ptocmslocal"
maxprocs="10"
command="java -jar ${JAR}"
datasets="wiki"
#datasets="lnorm"
#datasets="twitter"
datasets="zipf_10000000_1000000_12"
#datasets="time_ticker_tweets"
#initialTimestamp["time_ticker_tweets"]=1383264000
#initialTimestamp["twitter"]=1341791969 
#initialTimestamp["wiki"]=1199195421
#initialTimestamp["lnorm"]=1395928180
initialTimestamp["zipf_10000000_1000000_12"]=1422973590
#datasets="rlnorm1789_2366"
#datasets="rlnorm2245_1133"
#initialTimestamp["rlnorm2245_1133"]=1400168346
#initialTimestamp["rlnorm1789_2366"]=1400167617
#datasets="soc-Slashdot0811"
#initialTimestamp["soc-Slashdot0811"]=0
#datasets="soc-Slashdot0902"
#initialTimestamp["soc-Slashdot0902"]=0
#datasets="soc-LiveJournal1"
#initialTimestamp["soc-LiveJournal1"]=0


for data in $datasets; do
  input="${indir}"
for nc in $NumberOfChoices; do
  output="output_logs/dchoices/iteration5/${nc}/output"
  for ns in $NumberOfServers ; do   
   for nr in $NumberOfSources ; do
      echo "$command 11 ${input} ${output}_${ns}_${nr}_${nc}_${lbname} ${ns} ${initialTimestamp[$data]} ${nr} ${nc} " >> ${output}_${ns}_${nr}_${nc}_${lbname}.log
      cmdlines="$cmdlines $command 11 ${input} ${output}_${ns}_${nr}_${nc}_${lbname} ${ns} ${initialTimestamp[$data]} ${nr} ${nc} >> ${output}_${ns}_${nr}_${nc}_${lbname}.log;"
    done
  done
 done
done
#echo $cmdlinesi
echo -e $cmdlines | parallel --max-procs $maxprocs
echo "Done"
