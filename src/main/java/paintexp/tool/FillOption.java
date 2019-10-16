package paintexp.tool;

enum FillOption {
    FILL,
    STROKE,
    STROKE_FILL;

    public boolean isFill() {
        return this == FillOption.FILL || this == FillOption.STROKE_FILL;
    }

    public boolean isStroke() {
        return this == FillOption.STROKE || this == FillOption.STROKE_FILL;
    }

    @Override
    public String toString() {
        String lowerCase = super.toString().replaceAll("\\_F", " and F").toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }

}