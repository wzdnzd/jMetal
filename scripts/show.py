#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# @Author  : wzdnzd
# @Time    : 2021-08-26
# @Contact : hanxi2014@gmail.com
# @Project : jMetal
# @License : Copyright(C), BetaCat

import itertools
import functools
import re
import numpy as np
from numpy.lib.shape_base import tile
import pandas as pd
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
# import seaborn as sns
import os

# sns.set_theme(style="white")
plt.rcParams['font.sans-serif'] = ['Arial Unicode MS']

BASE_DIR = '/Users/Kevin/Downloads/jMetal'
DATA_DIR = os.path.join(BASE_DIR, 'dataTemp')
IMAGES_DIR = os.path.join(BASE_DIR, 'images')
ALGORITHM_NAME = 'ESPEA2'


# 按照population_size及max_iteration排序
def cmp(s1: str, s2: str):
    if not s1 or not s2:
        raise ValueError("字符串为空")

    arr1 = re.findall(r"\d+", s1)
    arr2 = re.findall(r"\d+", s2)

    if not arr1 or len(arr1) != len(arr2):
        raise ValueError("字符串格式不一致或不包含数字")

    result = 0
    for i in range(len(arr1)):
        diff = int(arr1[i]) - int(arr2[i])
        if diff != 0:
            result = -1 if diff < 0 else 1
            break

    return result


def precheck(path: str, filename: str) -> str:
    os.makedirs(path, exist_ok=True)
    filename = os.path.join(path, filename)
    if os.path.exists(filename) and os.path.isfile(filename):
        os.remove(filename)

    return filename


def algorithm_rename(name: str) -> str:
    if name == 'SPEA2AGA':
        name = ALGORITHM_NAME

    return name


def fetch_arguments(excludes: list = []) -> dict:
    arrays = sorted(os.listdir(DATA_DIR), key=functools.cmp_to_key(cmp))
    arguments = {}
    for p in arrays:
        key = os.path.join(DATA_DIR, p)
        numbers = re.findall(r"\d+", p)
        value = "/".join(numbers)
        if value in excludes:
            continue

        arguments[key] = value

    return arguments


def generate_indicator(args: dict,
                       indicator_name: str = 'IGD') -> pd.DataFrame:
    if not args:
        raise ValueError("参数列表为空，无法获取{}数据".format(indicator_name))

    columns = ['algorithm', 'arguments', 'mean', 'median']
    data = pd.DataFrame(columns=columns)
    for k, v in args.items():
        file = os.path.join(k, 'QualityIndicatorSummary.csv')
        if not os.path.exists(file) or not os.path.isfile(file):
            raise ValueError("文件{}不存在".format(file))

        df = pd.read_csv(file).drop(['Problem'], axis=1)
        df = df[df['IndicatorName'] == indicator_name]
        for algorithm, group in df.groupby('Algorithm'):
            indicator = group['IndicatorValue']
            mean = np.mean(indicator)
            median = np.median(indicator)
            data.loc[data.shape[0] + 1] = {
                'algorithm': algorithm_rename(algorithm),
                'arguments': v,
                'mean': mean,
                'median': median
            }

    return data


def read_coverage(args: dict) -> pd.DataFrame:
    if not args:
        raise ValueError("参数列表为空，无法获取C指标数据")

    columns = ['arguments', 'algorithm-1', 'algorithm-2', 'c']
    data = pd.DataFrame(columns=columns)
    for k, v in args.items():
        subpath = os.path.join(k, 'referenceFronts')
        if not os.path.exists(subpath) or not os.path.isdir(subpath):
            raise ValueError("文件夹{}不存在".format(subpath))

        for f in os.listdir(subpath):
            if f.endswith('.Coverage.csv'):
                df = pd.read_csv(os.path.join(subpath, f),
                                 names=['algorithm-1', 'algorithm-2', 'c'],
                                 sep=',')
                for _, row in df.iterrows():
                    data.loc[data.shape[0] + 1] = {
                        'arguments': v,
                        'algorithm-1': algorithm_rename(row['algorithm-1']),
                        'algorithm-2': algorithm_rename(row['algorithm-2']),
                        'c': row['c'],
                    }

    return data


def draw_pareto_front(subpath: str,
                      file: str = 'referenceFronts/OilScheduleProblem.csv',
                      image_name: str = 'pareto-front.png',
                      title: str = '原油短期调度问题帕累托面'):
    def scatter(df: pd.DataFrame, path: str, filename: str):
        if df is None or df.empty:
            raise ValueError("cannot scatter because dataframe is empty")

        fig = plt.figure(figsize=(14, 12))
        ax = Axes3D(fig=fig, auto_add_to_figure=False)
        fig.add_axes(ax)
        ax.set_title(title, fontdict={'weight': 'normal', 'size': 20})

        g = ax.scatter(df[df.columns[0]],
                       df[df.columns[1]],
                       df[df.columns[2]],
                       c=df[df.columns[3]],
                       marker='o',
                       s=np.power(1.0225, df[df.columns[4]]),
                       depthshade=False,
                       cmap='Paired',
                       alpha=0.75)

        ax.set_xticks(df[df.columns[0]].unique())
        ax.xaxis.set_tick_params(labelsize=15)
        ax.set_xlabel('${J_α}$', fontdict={'weight': 'normal', 'size': 18})
        ax.yaxis.set_tick_params(labelsize=15)
        ax.set_ylabel('${J_β}$', fontdict={'weight': 'normal', 'size': 18})
        ax.zaxis.set_tick_params(labelsize=15)
        ax.set_zlabel('${J_γ}$', fontdict={'weight': 'normal', 'size': 18})

        handles, l1 = g.legend_elements(prop="colors")
        # legend1 = ax.legend(handles, l1, loc="lower center", title="J_δ", ncol=len(l1))
        legend1 = ax.legend(handles,
                            l1,
                            loc="right",
                            title="${J_δ}$",
                            title_fontsize=18,
                            scatterpoints=1,
                            handlelength=4.5,
                            handletextpad=0.5,
                            bbox_to_anchor=(1.1, 0.65))
        ax.add_artist(legend1)

        # handles, l2 = g.legend_elements(prop="sizes", alpha=0.6)
        handles, _ = g.legend_elements(prop="sizes", alpha=0.6)
        # l2 = sorted(df[df.columns[4]].astype(np.int).unique().tolist())

        arrays = df[df.columns[4]].astype(int)

        upper = arrays.max()
        lower = arrays.min()

        l2 = range(lower, upper + 1)
        legend2 = ax.legend(handles,
                            l2,
                            loc="right",
                            title="${J_ε}$",
                            title_fontsize=18,
                            scatterpoints=1,
                            labelspacing=0.8,
                            handlelength=3.5,
                            handleheight=3,
                            handletextpad=1,
                            bbox_to_anchor=(1.1, 0.4))
        ax.add_artist(legend2)

        if not path or not filename:
            plt.show()
        else:
            filename = precheck(path, filename)
            plt.savefig(filename,
                        dpi=300,
                        bbox_inches='tight',
                        bbox_extra_artists=[legend1, legend2])

    subpath = os.path.join(DATA_DIR, subpath, file)
    if not os.path.exists(subpath):
        raise Exception("file {} not found".format(subpath))

    columns = ['o_' + str(x) for x in range(5)]
    df = pd.read_csv(subpath, names=columns, sep=',')
    scatter(df, IMAGES_DIR, image_name)


def draw_coverage(arguments: dict):
    def cmp_group(g1, g2):
        return cmp(g1[0], g2[0])

    def plot_bar(df: pd.DataFrame, algo_name: str, path: str, filename: str):
        if df is None or df.empty:
            raise ValueError("cannot bar because dataframe is empty")

        algorithms = df['algorithm-1'].unique()
        if algo_name not in algorithms:
            raise ValueError("not contains algorithm {}".format(algo_name))

        # 对分组后的实验数据按种群数和迭代次数进行排序
        groups = sorted(df.groupby('arguments'),
                        key=functools.cmp_to_key(cmp_group))
        ncols = 3
        nrows = int(np.ceil(len(groups) / ncols))

        width = 0.35
        fig = plt.figure(figsize=(32, 24))
        fig.subplots_adjust(hspace=0.3, wspace=0.2)

        i = 1
        for args, group in groups:
            ax = fig.add_subplot(nrows, ncols, i)

            d1 = group[group['algorithm-1'] == algo_name]
            d2 = group[group['algorithm-2'] == algo_name]
            x = np.arange(len(d1))

            # rects1 = ax.bar(x - width / 2, d1['c'], width, label=algo_name)
            # rects2 = ax.bar(x + width / 2, d2['c'], width, label='Others')

            ax.bar(x - width / 2, d1['c'], width, label=algo_name)
            ax.bar(x + width / 2, d2['c'], width, label='Others')

            lables = []
            lables.extend(d1['algorithm-1'])
            lables.extend(d2['algorithm-1'])

            # for tmp in zip(d1['algorithm-1'], d2['algorithm-1']):
            #     lables.extend(tmp)

            ax.set_ylabel('C指标', fontdict={'weight': 'normal', 'size': 18})
            ax.yaxis.set_tick_params(labelsize=15)
            words = args.split('/')
            ax.set_title('种群:{}    迭代次数:{}'.format(words[0], words[1]),
                         fontdict={
                             'weight': 'normal',
                             'size': 20
                         })
            ax.set_xticks(np.append(x - width / 2, x + width / 2))
            ax.set_xticklabels(lables, rotation=30, fontsize=15)

            # ax.set_xticks(x)
            # ax.set_xticklabels(lables)
            # ax.legend()

            # 显示条形图数字
            # ax.bar_label(rects1, padding=3)
            # ax.bar_label(rects2, padding=3)

            i += 1

        if not path or not filename:
            plt.show()
        else:
            filename = precheck(path, filename)
            plt.savefig(filename, dpi=300, bbox_inches='tight')

    df = read_coverage(arguments)
    plot_bar(df, ALGORITHM_NAME, IMAGES_DIR, 'c-indicator.png')


def draw_indicator(arguments: dict, indicator: str, title: str):
    def plot(df: pd.DataFrame, indicator: str, path: str, filename: str,
             title: str, ylable: str):
        if df is None or df.empty:
            raise ValueError("cannot plot because dataframe is empty")

        markers = itertools.cycle(('X', '1', 'v', 'o', '*', '|'))
        lables = arguments.values()
        plt.figure(figsize=(14, 8))
        for algorithm, group in df.groupby('algorithm'):
            plt.plot(range(len(group)),
                     group[indicator],
                     label=algorithm,
                     marker=next(markers))

        plt.title(title, fontdict={'weight': 'normal', 'size': 20})
        plt.xticks(range(len(lables)), lables, fontsize=15)
        plt.xlabel("种群数/迭代次数", fontdict={'weight': 'normal', 'size': 15})
        plt.yticks(fontsize=16)
        plt.ylabel(ylable, fontdict={'weight': 'normal', 'size': 15})

        legend = plt.legend(title="算法名称",
                            loc="upper right",
                            title_fontsize=15,
                            bbox_to_anchor=(1.1, 1.01),
                            numpoints=1)
        if not path or not filename:
            plt.show()
        else:
            filename = precheck(path, filename)
            plt.savefig(filename,
                        dpi=300,
                        bbox_inches='tight',
                        bbox_extra_artists=[legend])

    df = generate_indicator(args=arguments, indicator_name=indicator)
    plot(df, 'mean', IMAGES_DIR, '{}-mean.png'.format(indicator.lower()),
         title, indicator)
    plot(df, 'median', IMAGES_DIR, '{}-median.png'.format(indicator.lower()),
         title, indicator)


if __name__ == "__main__":
    excludes = ['50/300', '50/500', '50/1000']
    arguments = fetch_arguments(excludes=excludes)
    indicators = ["IGD", "IGD+", "GD", "NHV", "HV (PISA)"]
    for indicator in indicators:
        draw_indicator(arguments, indicator, "{}指标".format(indicator))

    draw_coverage(arguments)

    draw_pareto_front(subpath='OilScheduleStudy_Popsize200&Iteration_1000')
    draw_pareto_front(subpath='OilScheduleStudy_Popsize200&Iteration_1000',
                      file='referenceFronts/OilScheduleProblem.SPEA2AGA.rf',
                      image_name='espea2-pareto-front.png',
                      title='原油短期调度问题ESPEA2非支配解集')
