package moe.evoke.application.views.statistics;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.Pair;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;

@Route(value = "statistics", layout = MainView.class)
@CssImport("./views/statistics/statistics-view.css")
public class Statistics extends VerticalLayout implements HasDynamicTitle {

    private final Database database;

    public Statistics(@Autowired Database database) {
        addClassName("statistics-view");

        this.database = database;

        List<Pair<Date, Integer>> availableAnime = database.getStatsAvailableAnime();
        List<Pair<Date, Integer>> activeUsers = database.getStatsActiveUsers();
        List<Pair<Date, Integer>> watchedEpisodes = database.getStatsWatchedEpisodes();


        FlexLayout statsLayout = new FlexLayout();
        statsLayout.setJustifyContentMode(JustifyContentMode.START);
        statsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsLayout.setAlignContent(FlexLayout.ContentAlignment.CENTER);
        statsLayout.addClassName("stats-layout");
        add(statsLayout);

        ApexCharts availableAnimeChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.area)
                        .withZoom(ZoomBuilder.get()
                                .withEnabled(false)
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(new Series<>("Available Anime", availableAnime.stream().map(Pair::getSecond).toArray(Integer[]::new)))
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Available Anime")
                        .withAlign(Align.left).build())
                .withSubtitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLabels(availableAnime.stream().map(val -> val.getFirst().toString()).toArray(String[]::new))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(true).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        availableAnimeChart.addClassName("stats-chart");
        statsLayout.add(availableAnimeChart);

        ApexCharts activeUsersChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.area)
                        .withZoom(ZoomBuilder.get()
                                .withEnabled(false)
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(new Series<>("Active Users", activeUsers.stream().map(Pair::getSecond).toArray(Integer[]::new)))
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Active Users")
                        .withAlign(Align.left).build())
                .withSubtitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLabels(activeUsers.stream().map(val -> val.getFirst().toString()).toArray(String[]::new))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(true).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        activeUsersChart.addClassName("stats-chart");
        statsLayout.add(activeUsersChart);

        ApexCharts watchedAnimeChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.area)
                        .withZoom(ZoomBuilder.get()
                                .withEnabled(false)
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(new Series<>("Watched Episodes last 30 days", watchedEpisodes.stream().map(Pair::getSecond).toArray(Integer[]::new)))
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Watched Episodes last 30 days")
                        .withAlign(Align.left).build())
                .withSubtitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLabels(watchedEpisodes.stream().map(val -> val.getFirst().toString()).toArray(String[]::new))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(true).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        watchedAnimeChart.addClassName("stats-chart");
        statsLayout.add(watchedAnimeChart);
    }

    @Override
    public String getPageTitle() {
        return "Statistics";
    }
}

