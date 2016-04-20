reset
set terminal postscript eps size 3,1.5 enhanced font 'Helvetica,18'
set output "utility-fun.eps"

set border linewidth 1.5
set style line 1 lc rgb '#0060ad' lt 1 lw 3 # --- blue

set xrange [0:1000]
set yrange [0:1]

# set logscale y

set ylabel "Penalty\n(1-Utility)" offset 1.5,0
set xlabel "Latency"

# set key out top left height 0.9
# set key horizontal maxrows 2 samplen 2
unset key

set grid
# set xtics("50" 0.05, "100" 0.1, "150" 0.15, "200" 0.2, "250" 0.25, "300" 0.3)

unset border


stp = 5
penalty(x) = (1 - exp(-x*8/1000))**stp

plot penalty(x) w lines ls 1