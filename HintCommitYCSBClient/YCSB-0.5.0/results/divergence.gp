reset

set terminal postscript eps size 3.7,2 enhanced font 'Helvetica,18'
set output "divergence.eps"

set border linewidth 1.5
set style line 1 lc rgb '#0060ad' lt 10 pt 5 lw 2 ps 2 # --- blue
set style line 2 lc rgb '#666666' lt 11 pt 31 lw 2 ps 2 # --- gray
set style line 3 lc rgb '#0060ad' lt 10 pt 4 lw 2 ps 2 # --- blue
set style line 4 lc rgb '#666666' lt 11 pt 6 lw 2 ps 2 # --- blue

set style line 12 lc rgb '#ddccdd' lt 3 lw 1.5

set xrange [7:105]
set yrange [-0.5:27]

set ylabel "%Divergence" offset 1.2,0
set xlabel "#Clients" offset 0,0.5

# set logscale y

set key out top left height 0.9
set key horizontal maxrows 2 samplen 2

set grid y mytics ls 12
set xtics ("10" 10,"20" 20,"40" 40, "60" 60, "80" 80, "80" 80, "100" 100)
set mytics 2

plot "data/a-latest.data" u 1:2 w linespoints ls 1 t "A (latest)",\
     "data/a-uniform.data" u 1:2 w linespoints ls 2 t "A (uniform)",\
    "data/b-latest.data" u 1:2 w linespoints ls 3 t "B (latest)",\
    "data/b-uniform.data" u 1:2 w linespoints ls 4 t "B (uniform)"
