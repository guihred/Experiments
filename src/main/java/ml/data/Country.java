package ml.data;

import static ml.data.Continent.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.RunnableEx;

public enum Country {
    // ASIA
    YE(ASIA, "Yemen", "YE"),
    HK(ASIA, "Hong Kong|Hong Kong SAR, China", "HK"),
    SG(ASIA, "Singapore", "SG"),
    JP(ASIA, "Japan", "JP"),
    KZ(ASIA, "Kazakhstan", "KZ"),
    AF(ASIA, "Afghanistan", "AF"),
    AE(ASIA, "United Arab Emirates", "AE"),
    AZ(ASIA, "Azerbaijan", "AZ"),
    IN(ASIA, "India", "IN"),
    BD(ASIA, "Bangladesh", "BD"),
    IL(ASIA, "Israel", "IL"),
    LB(ASIA, "Lebanon", "LB"),
    LK(ASIA, "Sri Lanka", "LK"),
    MM(ASIA, "Myanmar", "MM"),
    MN(ASIA, "Mongolia", "MN"),
    KP(ASIA, "Dem. Rep. Korea|Korea, Dem. People’s Rep.", "KP"),
    PS(ASIA, "Palestine", "PS"),
    QA(ASIA, "Qatar", "QA"),
    RU(ASIA, "Russia|Russian Federation", "RU"),
    SA(ASIA, "Saudi Arabia", "SA"),
    SY(ASIA, "Syria|Syrian Arab Republic", "SY"),
    TH(ASIA, "Thailand", "TH"),
    TJ(ASIA, "Tajikistan", "TJ"),
    TM(ASIA, "Turkmenistan", "TM"),
    JO(ASIA, "Jordan", "JO"),
    MY(OCEANIA, "Malaysia", "MY"),
    NP(ASIA, "Nepal", "NP"),
    PK(ASIA, "Pakistan", "PK"),
    VN(ASIA, "Vietnam", "VN"),
    LA(ASIA, "Lao PDR", "LA"),
    AM(ASIA, "Armenia", "AM"),
    GE(ASIA, "Georgia", "GE"),
    BT(ASIA, "Bhutan", "BT"),
    OM(ASIA, "Oman", "OM"),
    IR(ASIA, "Iran|Iran, Islamic Rep.", "IR"),
    CY(ASIA, "Cyprus", "CY"),
    KG(ASIA, "Kyrgyzstan|Kyrgyz Republic", "KG"),
    KH(ASIA, "Cambodia", "KH"),
    KR(ASIA, "Korea|Korea, Rep.", "KR"),
    KW(ASIA, "Kuwait", "KW"),
    TR(ASIA, "Turkey", "TR"),
    TW(ASIA, "Taiwan", "TW"),
    UZ(ASIA, "Uzbekistan", "UZ"),
    IQ(ASIA, "Iraq", "IQ"),
    CN(ASIA, "China|China[\\(People's Republic of\\)]*", "CN"),
    // AFRICA
    TN(AFRICA, "Tunisia", "TN"),
    UG(AFRICA, "Uganda", "UG"),
    DZ(AFRICA, "Algeria", "DZ"),
    AO(AFRICA, "Angola", "AO"),
    BW(AFRICA, "Botswana", "BW"),
    CF(AFRICA, "Central African Rep.|Central African Republic", "CF"),
    KE(AFRICA, "Kenya", "KE"),
    CI(AFRICA, "Côte d'Ivoire|Cote d'Ivoire", "CI"),
    EG(AFRICA, "Egypt|Egypt, Arab Rep.", "EG"),
    LR(AFRICA, "Liberia", "LR"),
    LS(AFRICA, "Lesotho", "LS"),
    NA(AFRICA, "Namibia", "NA"),
    NE(AFRICA, "Niger", "NE"),
    NG(AFRICA, "Nigeria", "NG"),
    MG(AFRICA, "Madagascar", "MG"),
    ML(AFRICA, "Mali", "ML"),
    MZ(AFRICA, "Mozambique", "MZ"),
    MR(AFRICA, "Mauritania", "MR"),
    MA(AFRICA, "Morocco", "MA"),
    RW(AFRICA, "Rwanda", "RW"),
    EH(AFRICA, "W. Sahara", "EH"),
    SD(AFRICA, "Sudan", "SD"),
    SS(AFRICA, "S. Sudan|South Sudan", "SS"),
    SN(AFRICA, "Senegal", "SN"),
    SL(AFRICA, "Sierra Leone", "SL"),
    SZ(AFRICA, "Swaziland", "SZ"),
    LY(AFRICA, "Libya", "LY"),
    CG(AFRICA, "Congo|Congo, Rep.", "CG"),
    TZ(AFRICA, "Tanzania", "TZ"),
    GN(AFRICA, "Guinea", "GN"),
    TD(AFRICA, "Chad", "TD"),
    BF(AFRICA, "Burkina Faso", "BF"),
    ER(AFRICA, "Eritrea", "ER"),
    GM(AFRICA, "Gambia|Gambia, The", "GM"),
    GW(AFRICA, "Guinea-Bissau", "GW"),
    CM(AFRICA, "Cameroon", "CM"),
    ZA(AFRICA, "South Africa", "ZA"),
    ZM(AFRICA, "Zambia", "ZM"),
    ZW(AFRICA, "Zimbabwe", "ZW"),
    SO(AFRICA, "Somalia", "SO"),
    CV(AFRICA, "Cape Verde|Cabo Verde", "CV"),
    ST(AFRICA, "São Tomé and Principe|Sao Tome and Principe", "ST"),
    CD(AFRICA, "Dem. Rep. Congo|Congo, Dem. Rep.", "CD"),
    ET(AFRICA, "Ethiopia", "ET"),
    // EUROPE
    LT(EUROPE, "Lithuania", "LT"),
    LU(EUROPE, "Luxembourg", "LU"),
    LV(EUROPE, "Latvia", "LV"),
    IS(EUROPE, "Iceland", "IS"),
    IT(EUROPE, "Italy", "IT"),
    IE(EUROPE, "Ireland", "IE"),
    CH(EUROPE, "Switzerland", "CH"),
    UA(EUROPE, "Ukraine", "UA"),
    BA(EUROPE, "Bosnia and Herz.|Bosnia and Herzegovina", "BA"),
    HR(EUROPE, "Croatia", "HR"),
    HU(EUROPE, "Hungary", "HU"),
    CZ(EUROPE, "Czech Republic", "CZ"),
    BG(EUROPE, "Bulgaria", "BG"),
    PT(EUROPE, "Portugal", "PT"),
    RO(EUROPE, "Romania", "RO"),
    RS(EUROPE, "Serbia", "RS"),
    SK(EUROPE, "Slovakia|Slovakia Republic|Slovak Republic", "SK"),
    SI(EUROPE, "Slovenia", "SI"),
    MD(EUROPE, "Moldova", "MD"),
    ME(EUROPE, "Montenegro", "ME"),
    NL(EUROPE, "Netherlands", "NL"),
    NO(EUROPE, "Norway", "NO"),
    PL(EUROPE, "Poland", "PL"),
    SE(EUROPE, "Sweden", "SE"),
    AT(EUROPE, "Austria", "AT"),
    AL(EUROPE, "Albania", "AL"),
    BY(EUROPE, "Belarus", "BY"),
    MK(EUROPE, "Macedonia|Macedonia, FYR", "MK"),
    EE(EUROPE, "Estonia", "EE"),
    GB(EUROPE, "United Kingdom", "GB"),
    BE(EUROPE, "Belgium", "BE"),
    GR(EUROPE, "Greece", "GR"),
    FR(EUROPE, "France", "FR"),
    ES(EUROPE, "Spain", "ES"),
    LI(EUROPE, "Liechtenstein", "LI"),
    DK(EUROPE, "Denmark", "DK"),
    FI(EUROPE, "Finland", "FI"),
    DE(EUROPE, "Germany", "DE"),
    // NORTH_AMERICA
    MX(NORTH_AMERICA, "Mexico", "MX"),
    GL(NORTH_AMERICA, "Greenland", "GL"),
    CU(NORTH_AMERICA, "Cuba", "CU"),
    NI(NORTH_AMERICA, "Nicaragua", "NI"),
    PA(NORTH_AMERICA, "Panama", "PA"),
    US(NORTH_AMERICA, "United States", "US"),
    DO(NORTH_AMERICA, "Dominican Rep.|Dominican Republic", "DO"),
    CA(NORTH_AMERICA, "Canada", "CA"),
    AI(NORTH_AMERICA, "Anguilla", "AI"),
    AG(NORTH_AMERICA, "Antigua and Barb.|Antigua and Barbuda", "AG"),
    BS(NORTH_AMERICA, "Bahamas|Bahamas, The", "BS"),
    BM(NORTH_AMERICA, "Bermuda", "BM"),
    BB(NORTH_AMERICA, "Barbados", "BB"),
    PR(NORTH_AMERICA, "Puerto Rico", "PR"),
    TT(NORTH_AMERICA, "Trinidad and Tobago", "TT"),
    CR(NORTH_AMERICA, "Costa Rica", "CR"),
    HT(NORTH_AMERICA, "Haiti", "HT"),
    GT(NORTH_AMERICA, "Guatemala", "GT"),
    JM(NORTH_AMERICA, "Jamaica", "JM"),
    SV(NORTH_AMERICA, "El Salvador", "SV"),
    // SOUTH_AMERICA
    AR(SOUTH_AMERICA, "Argentina", "AR"),
    CO(SOUTH_AMERICA, "Colombia", "CO"),
    BO(SOUTH_AMERICA, "Bolivia", "BO"),
    BR(SOUTH_AMERICA, "Brazil", "BR"),
    GY(SOUTH_AMERICA, "Guyana", "GY"),
    EC(SOUTH_AMERICA, "Ecuador", "EC"),
    CL(SOUTH_AMERICA, "Chile", "CL"),
    SR(SOUTH_AMERICA, "Suriname", "SR"),
    UY(SOUTH_AMERICA, "Uruguay", "UY"),
    VE(SOUTH_AMERICA, "Venezuela", "VE"),
    PY(SOUTH_AMERICA, "Paraguay", "PY"),
    GF(SOUTH_AMERICA, "French Guiana", "GF"),
    FO(SOUTH_AMERICA, "Faeroe Is.|Faroe Islands", "FO"),
    FK(SOUTH_AMERICA, "Falkland Is.", "FK"),
    PE(SOUTH_AMERICA, "Peru", "PE"),
    // OCEANIA
    AU(OCEANIA, "Australia", "AU"),
    ID(OCEANIA, "Indonesia", "ID"),
    NZ(OCEANIA, "New Zealand", "NZ"),
    PH(OCEANIA, "Philippines", "PH"),
    PG(OCEANIA, "Papua New Guinea", "PG"),
    TL(OCEANIA, "Timor-Leste", "TL"),
    BN(OCEANIA, "Brunei|Brunei Darussalam", "BN"),
    PF(OCEANIA, "Fr. Polynesia|French Polynesia", "PF"),
    // ANTARCTICA
    MW(AFRICA, "Malawi", "MW"),
    TG(AFRICA, "Togo", "TG"),
    KY(NORTH_AMERICA, "Cayman Is.|Cayman Islands", "KY"),
    VU(OCEANIA, "Vanuatu", "VU"),
    BI(AFRICA, "Burundi", "BI"),
    BJ(AFRICA, "Benin", "BJ"),
    DJ(AFRICA, "Djibouti", "DJ"),
    BZ(NORTH_AMERICA, "Belize", "BZ"),
    FJ(OCEANIA, "Fiji", "FJ"),
    GA(AFRICA, "Gabon", "GA"),
    GH(AFRICA, "Ghana", "GH"),
    HN(NORTH_AMERICA, "Honduras", "HN"),
    GQ(AFRICA, "Equatorial Guinea|Eq. Guinea", "GQ"),
    AW(NORTH_AMERICA, "Aruba", "AW"),
    AD(EUROPE, "Andorra", "AD"),
    KM(AFRICA, "Comoros", "KM"),
    DM(NORTH_AMERICA, "Dominica", "DM"),
    GD(NORTH_AMERICA, "Grenada", "GD"),
    KN(NORTH_AMERICA, "St. Kitts and Nevis", "KN"),
    LC(NORTH_AMERICA, "Saint Lucia|St. Lucia", "LC"),
    MV(ASIA, "Maldives", "MV"),
    MT(EUROPE, "Malta", "MT"),
    MS(NORTH_AMERICA, "Montserrat", "MS"),
    MU(AFRICA, "Mauritius", "MU"),
    NC(OCEANIA, "New Caledonia", "NC"),
    NR(OCEANIA, "Nauru", "NR"),
    PN(OCEANIA, "Pitcairn Is.", "PN"),
    SB(OCEANIA, "Solomon Is.|Solomon Islands", "SB"),
    SX(NORTH_AMERICA, "Sint Maarten|Sint Maarten \\(Dutch part\\)", "SX"),
    SC(AFRICA, "Seychelles", "SC"),
    TC(NORTH_AMERICA, "Turks and Caicos Is.|Turks and Caicos Islands", "TC"),
    TO(OCEANIA, "Tonga", "TO"),
    VC(NORTH_AMERICA, "St. Vin. and Gren.|St. Vincent and the Grenadines", "VC"),
    VG(NORTH_AMERICA, "British Virgin Islands|British Virgin Is.", "VG"),
    VI(NORTH_AMERICA, "U.S. Virgin Is.", "VI"),
    RE(AFRICA, "Reunion", "RE"),
    YT(AFRICA, "Mayotte", "YT"),
    MQ(NORTH_AMERICA, "Martinique", "MQ"),
    GP(NORTH_AMERICA, "Guadeloupe", "GP"),
    CW(NORTH_AMERICA, "Curaco|Curacao", "CW"),
    IC(AFRICA, "Canary Islands", "IC");
    static {
        loadPaths();
    }

    private final String countryName;

    private final String code;
    private String path;
    private final Continent continent;

    private double[] center;

    private List<double[]> points = new ArrayList<>();

    private DoubleSummaryStatistics xStats;

    private DoubleSummaryStatistics yStats;

    private Set<Country> neighbors;

    Country(Continent continent, String name, String code) {
        this.continent = continent;
        countryName = name;
        this.code = code;
    }

    public double getCenterX() {
        if (center == null) {
            center = computeCentroid();
        }
        return center[0];
    }

    public double getCenterY() {
        if (center == null) {
            center = computeCentroid();
        }

        return center[1];
    }

    public String getCode() {
        return code;
    }

    public Continent getContinent() {
        return continent;
    }

    public String getCountryName() {
        return countryName.replaceAll("\\|.+", "");
    }

    public String getPath() {
        return path;
    }

    public List<double[]> getPoints() {
        if (points.isEmpty()) {
            String[] regions = Stream.of(path.split("[mz]")).filter(StringUtils::isNotBlank).toArray(String[]::new);
            double x = 0;
            double y = 0;
            for (int i = 0; i < regions.length; i++) {
                String[] pointPairs = regions[i].split(" ");
                for (int j = 0; j < pointPairs.length; j++) {
                    String[] xYCoord = pointPairs[j].split(",");
                    if (xYCoord.length == 2) {
                        x += Double.parseDouble(xYCoord[0]);
                        y += Double.parseDouble(xYCoord[1]);
                        points.add(new double[] { x, y });
                    }
                }
            }
            xStats = points.stream().mapToDouble(e -> e[0]).summaryStatistics();
            yStats = points.stream().mapToDouble(e -> e[1]).summaryStatistics();
        }
        return points;
    }

    public DoubleSummaryStatistics getxStats() {
        getPoints();
        return xStats;
    }

    public DoubleSummaryStatistics getyStats() {
        getPoints();
        return yStats;
    }

    public boolean matches(String name) {
        return name.matches(countryName);
    }

    public Set<Country> neighbors() {

        if (neighbors == null) {
            neighbors = new LinkedHashSet<>();
            Country[] values = values();
            for (int i = 0; i < values.length; i++) {
                Country country = values[i];
                if (country != this) {
                    DoubleSummaryStatistics countryX = country.getxStats();
                    DoubleSummaryStatistics countryY = country.getyStats();
                    if (intersect(countryX.getMin(), countryY.getMin(), countryX.getMax() - countryX.getMin(),
                            countryY.getMax() - countryY.getMin())) {

                        neighbors.add(country);
                        country.addNeighbor(this);
                    }
                }
            }

        }
        return neighbors;
    }

    private void addNeighbor(Country country) {
        if (neighbors != null) {
            neighbors.add(country);
        }
    }

    private double[] computeCentroid() {
        List<double[]> points1 = getPoints();
        double[] centroid = { 0, 0 };
        double signedArea = 0.0;
        double x0; // Current vertex X
        double y0; // Current vertex Y
        double x1; // Next vertex X
        double y1; // Next vertex Y
        double a; // Partial signed area

        // For all vertices except last
        int i = 0;
        for (i = 0; i < points1.size() - 1; ++i) {
            x0 = points1.get(i)[0];
            y0 = points1.get(i)[1];
            x1 = points1.get(i + 1)[0];
            y1 = points1.get(i + 1)[1];
            a = x0 * y1 - x1 * y0;
            signedArea += a;
            centroid[0] += (x0 + x1) * a;
            centroid[1] += (y0 + y1) * a;
        }

        // Do last vertex separately to avoid performing an expensive
        // modulus operation in each iteration.
        x0 = points1.get(i)[0];
        y0 = points1.get(i)[1];
        x1 = points1.get(0)[0];
        y1 = points1.get(0)[1];
        a = x0 * y1 - x1 * y0;
        signedArea += a;
        centroid[0] += (x0 + x1) * a;
        centroid[1] += (y0 + y1) * a;

        signedArea /= 2;
        centroid[0] /= 6.0 * signedArea;
        centroid[1] /= 6.0 * signedArea;

        return centroid;
    }

    private boolean intersect(double x, double y, double width, double height) {
        getPoints();
        return x + width >= xStats.getMin() && y + height >= yStats.getMin() && x <= xStats.getMax()
                && y <= yStats.getMax();

    }

    public static boolean hasName(String name) {
        return Stream.of(values()).anyMatch(e -> name.matches(e.countryName));
    }

    public static void loadPaths() {
        RunnableEx.run(() -> {
            File file = ResourceFXUtils.toFile("countries.csv");
            try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.displayName())) {
                while (scanner.hasNext()) {
                    String firstLine = StringSigaUtils.fixEncoding(scanner.nextLine()).replaceAll("\"", "");
                    String[] split = firstLine.split(";");
                    Country.valueOf(split[0]).path = split[1];
                }
            }
        });
    }

}
