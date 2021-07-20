/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */
package nl.b3p.jdbc.util.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InsertGeometryIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(InsertGeometryIntegrationTest.class);
    private static final String INSERT_STATEMENT = "INSERT INTO geometries (geom, naam) VALUES (%s, ?)";
    private Geometry geom = null;

static Stream<Arguments> featureProvider() {
    return Stream.of(
            arguments("test", 28992, "MULTIPOLYGON (((0 0, 10 0, 5 5, 0 0)))"),
            arguments("sloot", 28992, "MULTIPOLYGON (((238510.319 560225.328, 238509.942 560225.021, 238522.621 560213.046, 238537.167 560199.423, 238537.415 560199.191, 238539.784 560198.151, 238543.891 560193.921, 238552.898 560186.639, 238558.987 560183.617, 238560.947 560182.093, 238561.848 560180.481, 238562.623 560178.22, 238562.427 560176.072, 238561.409 560173.929, 238555.694 560162.32, 238551.821 560153.393, 238547.821 560144.089, 238539.619 560126.983, 238534.428 560116.124, 238528.784 560108.795, 238527.261 560098.877, 238527.954 560096.965, 238526.065 560079.053, 238523.789 560062.407, 238520.604 560053.256, 238517.119 560039.845, 238515.228 560027.656, 238515.372 560025.477, 238516.685 560022.118, 238519.73 560013.764, 238521.004 560009.246, 238522.181 560006.651, 238523.36 560005.312, 238524.35 560004.171, 238524.479 560004.041, 238526.717 560002.86, 238528.55 560000.958, 238534.171 559997.044, 238536.968 559994.21, 238539.376 559989.861, 238540.385 559986.164, 238539.773 559982.708, 238538.813 559979.717, 238535.943 559974.781, 238530.056 559967.52, 238522.452 559957.954, 238517.345 559951.762, 238502.739 559937.228, 238494.775 559929.976, 238489.548 559926.191, 238483.514 559920.837, 238480.819 559918.804, 238479.911 559917.359, 238479.427 559915.807, 238479.455 559914.939, 238478.987 559910.236, 238478.792 559908.361, 238479.369 559906.186, 238480.682 559904.082, 238482.836 559902.196, 238489.598 559896.056, 238496.768 559891.384, 238507.836 559885.944, 238513.119 559883.589, 238515.161 559881.715, 238516.825 559878.711, 238518.145 559875.191, 238518.176 559872.041, 238517.28 559867.219, 238516.377 559864.098, 238516.066 559862.929, 238515.587 559858.435, 238511.726 559846.403, 238510.25 559840.974, 238507.189 559832.2, 238503.774 559824.052, 238498.267 559812.468, 238493.094 559802.542, 238490.908 559799.344, 238489.309 559797.575, 238487.14 559796.565, 238483.98 559794.187, 238475.062 559790.214, 238472.721 559788.81, 238467.763 559786.985, 238464.063 559785.43, 238458.645 559782.826, 238449.674 559779.269, 238444.796 559776.821, 238441.305 559775.567, 238438.983 559774.115, 238436.7 559771.862, 238435.163 559769.517, 238434.292 559766.722, 238433.982 559763.329, 238434.269 559757.989, 238435.968 559753.364, 238439.153 559746.045, 238440.166 559741.642, 238440.542 559736.772, 238438.907 559727.138, 238437.177 559714.958, 238436.431 559709.05, 238435.875 559701.962, 238434.823 559690.7, 238434.967 559688.794, 238435.325 559685.226, 238434.752 559679.44, 238433.169 559668.135, 238430.344 559654.435, 238428.923 559643.14, 238427.495 559638.002, 238425.878 559634.045, 238425.22 559632.957, 238423.704 559629.979, 238417.496 559620.468, 238412.385 559612.475, 238410.059 559608.24, 238404.908 559598.814, 238404.537 559595.016, 238403.782 559585.507, 238403.718 559579.811, 238403.173 559574.363, 238403.88 559569.861, 238404.466 559568.543, 238406.451 559563.046, 238409.863 559555.708, 238413.05 559549.646, 238415.935 559544.775, 238417.546 559539.677, 238417.885 559537.411, 238417.634 559535.404, 238416.781 559533.817, 238414.228 559531.839, 238408.051 559528.391, 238401.591 559524.126, 238398.827 559522.829, 238396.438 559522.66, 238389.036 559519.553, 238382.014 559516.594, 238378.098 559512.938, 238376.336 559510.888, 238375.41 559509.56, 238375.428 559507.829, 238375.916 559505.543, 238376.723 559502.641, 238377.951 559497.123, 238378.082 559489.814, 238378.133 559480.892, 238378.418 559471.787, 238379.478 559466.421, 238380.917 559462.185, 238380.909 559458.854, 238381.421 559455.729, 238381.087 559454.071, 238379.971 559452.6, 238379.021 559451.683, 238376.037 559450.247, 238372.928 559449.415, 238370.472 559449.001, 238361.499 559446.426, 238357.012 559444.73, 238350.377 559441.813, 238331.412 559437.737, 238324.696 559435.388, 238313.976 559431.422, 238307.721 559430.125, 238303.299 559428.181, 238298.188 559427.168, 238292.64 559423.366, 238288.804 559420.343, 238284.602 559416.304, 238279.331 559410.811, 238277.313 559408.36, 238277.15 559408.161, 238275.139 559404.377, 238266.906 559394.184, 238263.367 559390.403, 238261.079 559388.583, 238256.642 559385.979, 238254.171 559384.633, 238251.615 559383.636, 238248.462 559383.331, 238241.193 559382.675, 238236.734 559383.062, 238235.822 559383.47, 238228.571 559387.346, 238222.008 559391.163, 238217.439 559393.088, 238212.154 559395.169, 238208.588 559396.34, 238200.919 559397.546, 238194.965 559398.548, 238189.57 559398.678, 238182.32 559397.975, 238175.447 559396.165, 238167.977 559394.068, 238161.157 559392.823, 238156.185 559391.591, 238151.499 559389.489, 238144.851 559386.131, 238143.161 559384.872, 238139.808 559383.285, 238135.737 559381.265, 238132.984 559380.355, 238131.227 559380.106, 238129.143 559379.019, 238127.298 559377.318, 238125.603 559375.238, 238124.741 559373.539, 238123.568 559371.936, 238121.576 559370.612, 238119.942 559369.212, 238118.817 559367.627, 238118.006 559365.512, 238118.121 559363.266, 238119.082 559359.552, 238121.323 559355.628, 238124.54 559350.886, 238128.161 559346.084, 238132.359 559341.341, 238136.131 559336.434, 238137.875 559332.807, 238139.727 559326.114, 238141.05 559319.379, 238141.615 559314.582, 238142.063 559310.504, 238141.679 559308.282, 238140.634 559306.347, 238139.321 559304.963, 238136.629 559302.221, 238123.407 559289.59, 238116.216 559282.313, 238108.774 559276.518, 238102.742 559272.419, 238095.364 559267.575, 238084.615 559261.036, 238071.831 559252.72, 238061.479 559247.534, 238051.31 559241.602, 238042.511 559235.932, 238035.44 559230.719, 238030.894 559226.164, 238026.857 559223.661, 238026.743 559223.493, 238020.814 559214.704, 238017.349 559211.007, 238012.859 559205.056, 238011.441 559202.267, 238007.803 559195.668, 238003.712 559189.109, 237999.804 559184.039, 237997.629 559182.209, 237995.516 559181.057, 237991.369 559179.11, 237981.089 559174.716, 237976.646 559173.527, 237976.478 559173.378, 237975.23 559172.266, 237970.55 559170.985, 237965.023 559169.918, 237957.747 559169.422, 237950.797 559167.528, 237948.688 559166.923, 237947.024 559165.183, 237944.03 559162.379, 237940.46 559158.259, 237928.379 559142.147, 237923.454 559136.261, 237921.587 559133.989, 237919.3 559132.442, 237913.681 559130.358, 237908.639 559129.862, 237901.642 559130.185, 237898.518 559128.692, 237895.341 559127.889, 237892.202 559125.737, 237889.158 559122.641, 237887.389 559120.75, 237887.096 559119.546, 237887.075 559114.575, 237884.804 559103.22, 237883.348 559098.017, 237881.936 559092.557, 237879.398 559086.496, 237875.214 559078.92, 237872.665 559073.999, 237868.583 559068.144, 237870.131 559068.483, 237872.307 559069.333, 237874.395 559070.966, 237876.465 559072.647, 237879.825 559078.702, 237883.479 559084.981, 237886.463 559091.163, 237887.427 559094.974, 237889.667 559110.733, 237889.946 559115.041, 237890.889 559117.373, 237892.722 559119.941, 237894.712 559122.245, 237896.893 559123.641, 237899.319 559125.244, 237908.263 559126.498, 237913.074 559126.468, 237916.189 559126.593, 237920.049 559128.154, 237923.795 559130.434, 237929.918 559135.277, 237937.618 559144.88, 237942.808 559152.25, 237946.273 559156.22, 237948.817 559159.339, 237951.009 559160.849, 237953.633 559162.364, 237958.399 559163.843, 237974.915 559167.071, 237982.599 559170.017, 237987.761 559171.595, 237990.798 559172.618, 237994.341 559174.436, 237997.567 559176.785, 238000.491 559179.071, 238003.459 559182.084, 238006.692 559186.509, 238013.294 559198.082, 238016.046 559202.483, 238020.337 559208.247, 238025.47 559214.232, 238031.171 559219.456, 238037.085 559225.254, 238047.86 559232.568, 238060.705 559241.562, 238070.236 559246.753, 238087.261 559255.797, 238098.419 559263.806, 238108.669 559270.369, 238118.459 559278.389, 238133.523 559291.194, 238138.212 559296.078, 238141.368 559299.164, 238143.738 559302.87, 238144.579 559305.325, 238145.518 559308.363, 238145.592 559311.663, 238144.793 559317.895, 238144.186 559322.238, 238143.054 559326.813, 238142.223 559331.451, 238139.989 559336.468, 238136.747 559342.671, 238134.981 559344.818, 238131.394 559349.253, 238129.518 559351.683, 238127.53 559354.397, 238125.898 559356.759, 238124.529 559360.26, 238124.163 559362.734, 238124.837 559365.615, 238126.131 559368.028, 238128.436 559370.782, 238131.523 559373.623, 238135.581 559376.079, 238142.36 559379.106, 238149.071 559381.89, 238162.919 559387.894, 238171.209 559389.984, 238179.215 559391.964, 238187.507 559393.074, 238202.642 559392.439, 238208.253 559390.921, 238212.902 559389.627, 238218.713 559387.315, 238224.761 559383.841, 238228.404 559381.773, 238234.583 559379.495, 238238.614 559378.67, 238241.405 559378.505, 238244.708 559378.704, 238248.277 559379.061, 238251.498 559379.609, 238254.322 559380.057, 238257.511 559381.246, 238263.292 559384.594, 238267.962 559388.271, 238272.513 559393.373, 238276.671 559398.922, 238278.593 559402.234, 238282.787 559407.689, 238284.833 559409.851, 238293.68 559417.775, 238297.554 559420.978, 238303.318 559423.39, 238310.614 559425.094, 238323.852 559430.15, 238340.734 559435.65, 238357.508 559440.451, 238375.812 559446.777, 238382.21 559449.383, 238384.051 559450.811, 238385.09 559452.198, 238385.565 559454.892, 238385.611 559458.128, 238385.797 559461.417, 238385.418 559466.013, 238383.471 559474.905, 238382.053 559501.559, 238381.509 559505.326, 238381.634 559508.212, 238381.95 559509.916, 238384.44 559512.197, 238391.76 559515.654, 238397.549 559518.84, 238402.201 559520.056, 238408.364 559522.843, 238412.569 559524.92, 238416.781 559527.819, 238419.623 559530.454, 238421.672 559533.163, 238422.545 559534.976, 238422.709 559537.766, 238421.949 559541.668, 238421.097 559544.825, 238418.726 559550.334, 238412.069 559561.368, 238409.509 559566.803, 238408.18 559569.229, 238407.778 559572.071, 238407.287 559574.442, 238407.197 559578.443, 238407.919 559586.085, 238408.718 559592.831, 238409.986 559597.962, 238411.56 559602.447, 238416.72 559610.731, 238419.696 559616.092, 238423.224 559622.976, 238425.532 559626.277, 238430.105 559631.878, 238434.994 559635.89, 238435.924 559640.857, 238437.627 559654.5, 238438.183 559662.568, 238439.449 559668.678, 238439.8 559676.013, 238442.009 559685.709, 238442.408 559691.807, 238443.013 559696.953, 238443.461 559704.596, 238444.325 559709.787, 238445.515 559717.066, 238447.186 559723.18, 238446.218 559724.756, 238444.989 559728.039, 238444.241 559732.328, 238444.309 559740.807, 238443.56 559745.094, 238442.789 559748.882, 238441.255 559752.809, 238440.377 559756.174, 238439.419 559759.181, 238439.272 559762.069, 238439.778 559765.101, 238440.59 559767.49, 238442.422 559770.06, 238444.465 559771.948, 238448.851 559774.26, 238457.559 559777.93, 238466.237 559781.262, 238481.782 559788.094, 238489.439 559792.5, 238492.799 559795.065, 238495.478 559798.674, 238500.558 559808.837, 238507.659 559822.624, 238513.715 559837.964, 238516.66 559845.221, 238520.22 559857.462, 238521.35 559865.318, 238522.036 559869.84, 238521.757 559873.765, 238521.274 559877.231, 238519.991 559880.655, 238517.747 559884.306, 238514.697 559887.367, 238497.762 559896.517, 238489.404 559902.415, 238486.625 559905.202, 238484.904 559907.093, 238484.101 559909.288, 238483.951 559911.628, 238484.606 559914.556, 238486.046 559916.59, 238489.183 559920.704, 238492.219 559922.981, 238494.643 559924.035, 238497.635 559926.567, 238510.812 559938.198, 238515.803 559942.873, 238520.297 559948.116, 238526.92 559956.427, 238535.132 559966.394, 238539.196 559972.178, 238541.731 559976.44, 238543.033 559979.947, 238543.832 559984.458, 238543.625 559987.92, 238542.113 559992.347, 238540.552 559995.228, 238535.19 560000.714, 238530.535 560004.95, 238527.448 560008.107, 238525.598 560010.329, 238523.776 560013.598, 238521.288 560019.825, 238520.026 560023.749, 238519.344 560026.755, 238519.012 560030.115, 238519.522 560033.695, 238523.845 560049.286, 238528.828 560068.353, 238531.38 560087.779, 238532.791 560098.963, 238532.886 560104.999, 238533.76 560108.065, 238536.89 560112.614, 238540.022 560118.416, 238543.552 560124.593, 238553.703 560145.786, 238560.868 560160.523, 238563.901 560166.997, 238565.875 560172.129, 238566.675 560175.386, 238567.113 560178.174, 238566.957 560179.967, 238566.301 560181.784, 238565.242 560183.66, 238563.344 560185.864, 238560.669 560187.545, 238555.879 560190.038, 238552.897 560192.365, 238531.828 560211.677, 238514.566 560228.784, 238510.319 560225.328)))")
    );
}

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    @DisplayName("Geometry Insert From Geom")
    @ParameterizedTest(name = "{index}: feature: {0}")
    @MethodSource("featureProvider")
    public void testGeometryInsertFromGeom(String geomName, int srid, String wktString) throws ParseException {
        geom = new WKTReader().read(wktString);
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(geom);

            PreparedStatement ps = c.prepareStatement(
                    String.format(INSERT_STATEMENT,  conv.createPSGeometryPlaceholder())
            );
            ps.setObject(1, o);
            ps.setString(2, geomName + 1);
            assertEquals(1, ps.executeUpdate(), "");
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }

    @DisplayName("Geometry Insert From Geom No Srid")
    @ParameterizedTest(name = "{index}: feature: {0}")
    @MethodSource("featureProvider")
    public void testGeometryInsertFromGeomNoSrid(String geomName, int srid, String wktString) throws ParseException {
        geom = new WKTReader().read(wktString);
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(geom, srid);

            PreparedStatement ps = c.prepareStatement(
                    String.format(INSERT_STATEMENT,  conv.createPSGeometryPlaceholder())
            );
            ps.setObject(1, o);
            ps.setString(2, geomName + 2);
            assertEquals(1, ps.executeUpdate(), "");
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }

    @DisplayName("Geometry Insert From String")
    @ParameterizedTest(name = "{index}: feature: {0}")
    @MethodSource("featureProvider")
    public void testGeometryInsertFromString(String geomName, int srid, String wktString) throws ParseException {
        try (Connection c = DriverManager.getConnection(
                params.getProperty("staging.jdbc.url"),
                params.getProperty("staging.user"),
                params.getProperty("staging.passwd"))) {

            GeometryJdbcConverter conv = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
            Object o = conv.convertToNativeGeometryObject(wktString);

            PreparedStatement ps = c.prepareStatement(
                    String.format(INSERT_STATEMENT,  conv.createPSGeometryPlaceholder())
            );
            ps.setObject(1, o);
            ps.setString(2, geomName + 2);
            assertEquals(1, ps.executeUpdate(), "");
        } catch (SQLException sqle) {
            fail("Insert failed, msg: " + sqle.getLocalizedMessage());
        }
    }
}
