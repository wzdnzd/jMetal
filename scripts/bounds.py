#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# @Author  : wzdnzd
# @Time    : 2021-08-01
# @Contact : hanxi2014@gmail.com
# @Project : jMetal
# @License : Copyright(C), BetaCat

import pandas as pd
import os

DATA_DIR = "/Users/Kevin/Downloads/jMetal/dataTemp"


def find_bound(subpath, file):
    file = os.path.join(DATA_DIR, subpath, 'referenceFronts', file)
    if not os.path.exists(file):
        raise Exception('file {} not exists'.format(file))

    columns = ['c_' + str(x) for x in range(5)]
    df = pd.read_csv(file, names=columns)

    print(os.path.join(subpath, file))
    print(df.describe())
    print()

    lowers = []
    uppers = []
    for col in columns:
        lowers.append(df[col].min())
        uppers.append(df[col].max())

    return lowers, uppers


if __name__ == "__main__":
    files = ['OilScheduleProblem.SPEA2.rf', 'OilScheduleProblem.SPEA2AGA.rf']
    subpaths = [
        'OilScheduleStudy_Popsize100&Iteration_300',
        'OilScheduleStudy_Popsize100&Iteration_500',
        'OilScheduleStudy_Popsize100&Iteration_1000',
        'OilScheduleStudy_Popsize150&Iteration_300',
        # 'OilScheduleStudy_Popsize150&Iteration_500',
        # 'OilScheduleStudy_Popsize150&Iteration_1000',
        # 'OilScheduleStudy_Popsize200&Iteration_300',
        # 'OilScheduleStudy_Popsize200&Iteration_500',
        # 'OilScheduleStudy_Popsize200&Iteration_1000'
    ]
    # for subpath in subpaths:
    #     for file in files:
    #         lowers, uppers = find_bound(subpath, file)
    #         print('path: {}\tmin: {}\t max: {}'.format(
    #             os.path.join(subpath, file), lowers, uppers))

    #     print()

    for subpath in subpaths:
        for file in files:
            find_bound(subpath, file)
