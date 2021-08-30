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


def dominated(x, y):
    return all(i <= j for i, j in zip(x, y)) & any(i < j for i, j in zip(x, y))


def find_dominated(subpath: str, fn1: str, fn2: str):
    fn1 = os.path.join(DATA_DIR, subpath, 'referenceFronts', fn1)
    if not os.path.exists(fn1):
        raise Exception('file {} not exists'.format(fn1))

    fn2 = os.path.join(DATA_DIR, subpath, 'referenceFronts', fn2)
    if not os.path.exists(fn2):
        raise Exception('file {} not exists'.format(fn2))

    columns = ['c_' + str(x) for x in range(5)]
    df1 = pd.read_csv(fn1, names=columns)
    df2 = pd.read_csv(fn2, names=columns)

    if df1 is None or df2 is None:
        raise Exception("dataframe is empty")

    for i in range(df1.shape[0]):
        v1 = df1.iloc[i].tolist()
        arrays = []
        for j in range(df2.shape[0]):
            v2 = df2.iloc[j].tolist()
            if dominated(v2, v1):
                arrays.append(v2)

        if len(arrays) > 0:
            print(v1, "\t", arrays)


if __name__ == "__main__":
    files = [
        'OilScheduleProblem.SPEA2AGA.NonDominated.rf',
        'OilScheduleProblem.SPEA2.NonDominated.rf'
    ]
    subpaths = [
        'OilScheduleStudy_Popsize100&Iteration_300',
        'OilScheduleStudy_Popsize100&Iteration_500',
        'OilScheduleStudy_Popsize100&Iteration_1000',
        'OilScheduleStudy_Popsize150&Iteration_300',
        'OilScheduleStudy_Popsize150&Iteration_500',
        'OilScheduleStudy_Popsize150&Iteration_1000',
        'OilScheduleStudy_Popsize200&Iteration_300',
        'OilScheduleStudy_Popsize200&Iteration_500',
        'OilScheduleStudy_Popsize200&Iteration_1000'
    ]

    for subpath in subpaths:
        print(">>>>> {} <<<<<".format(subpath))
        find_dominated(subpath, files[0], files[1])
