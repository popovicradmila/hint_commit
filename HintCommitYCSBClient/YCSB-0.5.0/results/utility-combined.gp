reset
set terminal postscript eps colortext size 3.7,2.3 enhanced font 'Helvetica,18'
set output "utility-combined.eps"

set macros

set border linewidth 1.5
set style line 1 lc rgb '#0060ad' lt 1 pt 8 lw 2 ps 1.5 # --- blue
set style line 2 lc rgb '#0060ad' lt 1 pt 10 lw 2 ps 1.5 # --- blue
set style line 3 lc rgb '#0060ad' lt 1 pt 9 lw 2 ps 1.5 # --- blue
set style line 4 lc rgb '#00000' lt 2 pt 4 lw 2 ps 1.5 # --- blue
set style line 5 lc rgb '#00000' lt 2 pt 6 lw 2 ps 1.5 # --- blue
set style line 6 lc rgb '#00000' lt 2 pt 5 lw 2 ps 1.5 # --- blue

set xrange [5:105]
set yrange [0.4:1]

# set logscale y

set ylabel "Utility" offset 1.5,0
# set xlabel "#Clients" offset 0,0.8


# hsq = 8
# stp = 3
# penalty(x) = (1 - exp(-((x+0.05)*hsq)**stp))



set style line 12 lc rgb '#ddccdd' lt 3 lw 1.5
set grid y ls 12

set xtics offset 0,0.19 rotate by -50
set xtics("30" 10, "60" 20, "120" 40, "180" 60, "240" 80, "300" 100)
set xtics scale 0.7 font 'Helvetica,15'

# set tics scale 0.5

stp = 5
penalty(x) = (1 - exp(-x*8/1000))**stp

# set style rect fc lt -1 fs solid 0.15 noborder
# set object 1 rect from screen 0.15,0.75 to screen 0.549,0.82 behind fs empty
# set object 2 rect from screen 0.55,0.75 to screen 0.95,0.82 behind fs empty


set label 1 at screen 0.35,.79 'Workload A' center front
set label 2 at screen 0.75,.79 'Workload B' center front
set label 3 at screen 0.55,0.05 '#Clients' center front

set label 4 at screen 0.25,.96 'Distribution "latest" ' center front
set label 5 at screen 0.23,.89 'Distribution "uniform"' center front

# horizontal separator
set object 3 rect from screen 0.05,0.92 to screen 0.95,0.921 behind fs empty

# vertical separator
set object 4 rect from screen 0.421,0.98 to screen 0.422,0.84 behind fs empty

set bmargin at screen 0.18
set tmargin at screen 0.75

set multiplot layout 1,2 rowsfirst font 'Helvetica,22'

set key out top left samplen 2 spacing 1.2
set key at screen 0.42,1 height .5
# set key at screen 100, screen 100
set key maxrows 2 vertical width 0.2

LMARGIN = "set lmargin at screen 0.15; set rmargin at screen 0.55"
RMARGIN = "set lmargin at screen 0.55; set rmargin at screen 0.95"

@LMARGIN

plot "data/a-latest.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 1 title "R=1",\
     "data/a-uniform.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 4 title "R=1",\
     "data/a-latest.data" u 1:(1-penalty($5/1000)) w lp ls 2 title "R=2",\
     "data/a-uniform.data" u 1:(1-penalty($5/1000)) w lp ls 5 title "R=2",\
     "data/a-latest.data" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 3 title "Flow",\
     "data/a-uniform.data" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 6 title "Flow"


unset label 2

set format y ''

unset ylabel
@RMARGIN

plot "data/b-latest.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 1 notitle,\
     "" u 1:(1-penalty($5/1000)) w lp ls 2 notitle,\
     "" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 3 notitle,\
     "data/b-uniform.data" u 1:(1-(($2/100)+penalty($4/1000))) w lp ls 4 notitle,\
     "" u 1:(1-penalty($5/1000)) w lp ls 5 notitle,\
     "" u 1:(1-(penalty($5/1000) * ($2/100) + penalty($4/1000))) w lp ls 6 notitle


unset multiplot


#ls 1,\ title "R=1 (latest)",\
#ls 2,\ title "R=2 (latest)",\
#ls 3,\ title "Flow (latest)",\
#ls 4,\ title "R=1 (uniform)",\
#ls 5,\ title "R=2 (uniform)",\
#ls 6  title "Flow (uniform)"