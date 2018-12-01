package paintexp.tool;

enum FillOption {
    FILL,
    STROKE,
    STROKE_FILL;

    @Override
    public String toString() {
        String lowerCase = super.toString().replaceAll("\\_F", " and F").toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }

}