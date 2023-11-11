# -*- coding: utf-8 -*-
"""
Created on Mon Jul 17 18:08:51 2023

@author: Sam
This script was created for filtering the data from Xue et al. 2020 (NIHMS1067226-supplement-Supplementary_Data_1).
"""
import pandas as pd


# Open data file.csv. Data including gene name and counts for each cell. Change path!
H358_all = pd.read_csv(r"path.csv", sep = ",")
H358_all.shape

print(H358_all.head)

# Generate a dataframe of the input data
df = pd.DataFrame(H358_all)

# Filter steps A=0h; B=4h; C=24h; D=72h
def columns_filter(H358_all):
    columns_filter0 = H358_all.filter(regex='(0|H358_A)')
    return columns_filter0

columns_filter0 = columns_filter(H358_all)
print(columns_filter0)

def columns_filter(H358_all):
    columns_filter1 = H358_all.filter(regex='(0|H358_B)')
    return columns_filter1

columns_filter1 = columns_filter(H358_all)
print(columns_filter1)

def columns_filter(H358_all):
    columns_filter2 = H358_all.filter(regex='(0|H358_C)')
    return columns_filter2

columns_filter2 = columns_filter(H358_all)
print(columns_filter2)

def columns_filter(H358_all):
    columns_filter3 = H358_all.filter(regex='(0|H358_D)')
    return columns_filter3

columns_filter3 = columns_filter(H358_all)
print(columns_filter3)

## Create a link to save the filtered outputs in a specific folder
output_pfad = r"path"

###Save the filtered DataFrame in a CSV file
columns_filter0.to_csv('columns_filter_H358_0h.csv', index=False)
columns_filter1.to_csv('columns_filter_H358_4h.csv', index=False)
columns_filter2.to_csv('columns_filter_H358_24h.csv', index=False)
columns_filter3.to_csv('columns_filter_H358_72h.csv', index=False)

##### end filtering step