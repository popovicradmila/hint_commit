reset
set terminal postscript eps size 3.7,2.3 enhanced font 'Helvetica,18'
set output "utility.eps"

set border linewidth 1.5
set style line 1 lc rgb '#0060ad' lt 1 pt 8 lw 2 ps 1.5 # --- blue
set style line 2 lc rgb '#0060ad' lt 1 pt 10 lw 2 ps 1.5 # --- blue
set style line 3 lc rgb '#0060ad' lt 1 pt 9 lw 2 ps 1.5 # --- blue
set style line 4 lc rgb '#00000' lt 2 pt 4 lw 2 ps 1.5 # --- blue
set style line 5 lc rgb '#00000' lt 2 pt 6 lw 2 ps 1.5 # --- blue
set style line 6 lc rgb '#00000' lt 2 pt 5 lw 2 ps 1.5 # --- blue

set xrange [5:105]
set yrange [0:1]

# set logscale y

set ylabel "Utility" offset 1.5,0
set xlabel "#Clients" offset 0,0.8

set key out top left height 0.9 width -2
set key horizontal maxrows 2 samplen 3

# hsq = 8
# stp = 3
# penalty(x) = (1 - exp(-((x+0.05)*hsq)**stp))

set style line 12 lc rgb '#ddccdd' lt 3 lw 1.5
set grid y ls 12

set xtics offset 0,0.3
set xtics("30" 10, "60" 20, "120" 40, "180" 60, "240" 80, "300" 100)

stp = 5
penalty(x) = (1 - exp(-x*8/1000))**stp



plot "data/a-latest.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 1 title "R=1 (latest)",\
     "" u 1:(1-penalty($5/1000)) w lp ls 2 title "R=2 (latest)",\
     "" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 3 title "Flow (latest)",\
     "data/a-uniform.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 4 title "R=1 (uniform)",\
     "" u 1:(1-penalty($5/1000)) w lp ls 5 title "R=2 (uniform)",\
     "" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 6 title "Flow (uniform)"