#!/bin/sh

echo "Using directory: $1"

TEMP="__parsebusy.tmp"
echo "Using temp: " ${TEMP}
> ${TEMP}

files=`find ${1} -type f | egrep 'data$'`
for f in ${files}; do

    num_div=`cat ${f} | grep -i 'divergent' | grep -i 'operations' |
        awk '{sum += $3;} END {if (sum > 0) printf "%.2f", sum/NR; else print 0}'`
    num_total=`cat ${f} | grep -i 'commit' | grep -i 'operations' |
        awk '{sum += $3;} END {printf "%.2f", sum/NR}'`

    unfound=`cat ${f} | grep -i 'commit' | grep -i 'NOT_FOUND' |
        awk '{sum += $3;} END {if (sum > 0) printf "%.2f", sum/NR; else print 0}'`

    num_found=`echo "${num_total} - ${unfound}" | bc -l`

    hint_avg=`cat ${f} | grep 'HINT' | grep 'AverageLatency' | awk '{sum += $3;} END {printf "%.2f", sum/NR}'`
    commit_avg=`cat ${f} | grep 'COMMIT' | grep 'AverageLatency' | awk '{sum += $3;} END {printf "%.2f", sum/NR}'`

    # let's get the workload configuration parameters from the file name

    threads=`basename ${f} | tr "-" " " | awk '{print $2}'`
    work=`basename ${f} | sed 's/client[0-9]\(.\).*/\1/'`
    distribution=`basename ${f} | tr "-" " " | tr "." " " | awk '{print $3}'`

    # printf "\n\t\t%s" `basename ${f}`
    # printf "\n# wk\tthreads\tdistrib\ttotal_reads\tdivergent_reads\thint_latency\tcommit_latency\n"

    printf "\n%s\t%s\t%s\t"  ${work} ${threads} ${distribution} >> ${TEMP}
    printf "%.2f\t%.2f\t%.2f\t%.2f" ${num_found} ${num_div} ${hint_avg} ${commit_avg} >> ${TEMP}
done


workloads=`cat ${TEMP} | cut -f1 | sort | uniq`

for wk in ${workloads}; do
    echo "Processing workload " ${wk}
    distributions=`cat ${TEMP} | grep "^${wk}" | cut -f3 | sort | uniq`
    for dstb in ${distributions}; do
        echo "distribution: " ${dstb}

        # now group by # of clients
        clts=`cat ${TEMP} | grep "^${wk}" | grep "${dstb}" | cut -f2 | sort -n | uniq`

        for cl in ${clts}; do
            percent=`cat ${TEMP} | grep "^${wk}\s${cl}\s${dstb}" | cut -f4- |
                awk '{t+=$1; d+=$2} END{printf "%.2f", (d*100)/t}'`
            latency_dif=`cat ${TEMP} | grep "^${wk}\s${cl}\s${dstb}" | cut -f4- |
                awk '{diff+=($4-$3)} END{printf "%.2f", diff/NR}'`

            hint_mean=`cat ${TEMP} | grep "^${wk}\s${cl}\s${dstb}" | cut -f4- |
                awk '{sum+=$3} END{printf "%.2f", sum/NR}'`
            commit_mean=`cat ${TEMP} | grep "^${wk}\s${cl}\s${dstb}" | cut -f4- |
                awk '{sum+=$4} END{printf "%.2f", sum/NR}'`

            # echo ${latency_dif}
            printf "%s\t%.5f\t%.5f\t%.5f\t%.5f\n" ${cl} ${percent} ${latency_dif} ${hint_mean} ${commit_mean} >> "./${wk}-${dstb}.data"            # printf "%s\t%.5f\t%.5f\t\t in %s" ${cl} ${percent} ${latency_dif} "./${1}-${wk}-${dstb}.data"
        done
        # exit;
        # echo "Clients" ${clts}
        # cat ${TEMP} | grep "^${wk}" | grep "${dstb}"
    done
    # exit;
done

unlink ${TEMP}