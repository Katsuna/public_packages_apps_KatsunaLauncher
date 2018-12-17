package com.katsuna.launcher.katsuna.dashboard.domain;

import android.content.Context;

import com.katsuna.launcher.R;


public enum WindDirection {
    // don't change order
    NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST,
    EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST,
    SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST,
    WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

    public static WindDirection byDegree(double degree) {
        return byDegree(degree, WindDirection.values().length);
    }

    public static WindDirection byDegree(double degree, int numberOfDirections) {
        WindDirection[] directions = WindDirection.values();
        int availableNumberOfDirections = directions.length;

        int direction = windDirectionDegreeToIndex(degree, numberOfDirections)
            * availableNumberOfDirections / numberOfDirections;

        return directions[direction];
    }

    // you may use values like 4, 8, etc. for numberOfDirections
    public static int windDirectionDegreeToIndex(double degree, int numberOfDirections) {
        // to be on the safe side
        degree %= 360;
        if (degree < 0) degree += 360;

        degree += 180 / numberOfDirections; // add offset to make North start from 0

        int direction = (int) Math.floor(degree * numberOfDirections / 360);

        return direction % numberOfDirections;
    }

    public String getLocalizedString(Context context) {
        // usage of enum.ordinal() is not recommended, but whatever
        return context.getResources().getStringArray(R.array.windDirections)[ordinal()];
    }

    public String getArrow(Context context) {
        // usage of enum.ordinal() is not recommended, but whatever
        return context.getResources().getStringArray(R.array.windDirectionArrows)[ordinal() / 2];
    }
}
