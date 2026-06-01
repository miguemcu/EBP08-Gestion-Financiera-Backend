package com.ebp08.gestion_financiera_backend.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import javax.imageio.ImageIO;

@Component
public class GraficaHelper {

    // Colores del diseño
    private static final Color COLOR_INGRESO  = new Color(30,  120, 200);
    private static final Color COLOR_EGRESO   = new Color(200, 50,  50);
    private static final Color COLOR_FONDO    = new Color(250, 250, 250);
    private static final Color COLOR_GRILLA   = new Color(220, 220, 220);

    /**
     * Genera gráfica de barras comparativa: Ingresos vs Gastos
     */
    public byte[] generarGraficaComparativa(BigDecimal totalIngresos,
                                             BigDecimal totalGastos,
                                             int mes, int anio) throws IOException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(totalIngresos, "Ingresos", "Ingresos");
        dataset.addValue(totalGastos,   "Gastos",   "Gastos");

        JFreeChart chart = ChartFactory.createBarChart(
                "Ingresos vs Gastos — " + mes + "/" + anio,
                null,
                "Monto ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, false, false
        );

        aplicarEstilo(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_INGRESO);
        renderer.setSeriesPaint(1, COLOR_EGRESO);

        // Ajuste adicional para centrar correctamente cuando hay pocas barras
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.0);

        return toBytes(chart, 500, 300);
    }

    /**
     * Genera gráfica de barras horizontales por categoría
     * @param datos mapa de nombreCategoria → monto, en orden
     * @param color color de las barras
     */
    public byte[] generarGraficaPorCategoria(Map<String, BigDecimal> datos,
                                              String titulo,
                                              Color color) throws IOException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, BigDecimal> entry : datos.entrySet()) {
            dataset.addValue(entry.getValue(), "Monto", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Categoría",
                "Monto ($)",
                dataset,
                PlotOrientation.HORIZONTAL,
                false, false, false
        );

        aplicarEstilo(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, color);

        // Alto dinámico según cantidad de categorías
        int alto = Math.max(200, datos.size() * 45);
        return toBytes(chart, 500, alto);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void aplicarEstilo(JFreeChart chart) {
        chart.setBackgroundPaint(COLOR_FONDO);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(40, 40, 40));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(COLOR_GRILLA);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setOutlineVisible(false);

        // Centrar barras correctamente
        plot.getDomainAxis().setCategoryMargin(0.4);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.15); // más angosto = más centrado
        renderer.setItemMargin(0.0);
        renderer.setShadowVisible(false);

        CategoryAxis ejeX = plot.getDomainAxis();
        ejeX.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        ejeX.setAxisLineVisible(false);

        NumberAxis ejeY = (NumberAxis) plot.getRangeAxis();
        ejeY.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        ejeY.setAxisLineVisible(false);
    }

    private byte[] toBytes(JFreeChart chart, int ancho, int alto) throws IOException {
        BufferedImage imagen = chart.createBufferedImage(ancho, alto);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(imagen, "PNG", out);
        return out.toByteArray();
    }
}
