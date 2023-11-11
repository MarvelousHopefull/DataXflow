
# Phyton script for normalization of singlecell-Data, here log-transformed counts.
# by Samantha A. W. Crouch 2023\09\18
# under Spyder (Phyton 3.9)

import pandas as pd
import numpy as np

# Open normalized counts and Jimena_node_output as cvs.files
H358_0 = pd.read_csv(r"path\columns_filter_H358_0h.csv", sep = ",")
H358_4 = pd.read_csv(r"path\columns_filter_H358_4h.csv", sep=",")
H358_24 = pd.read_csv(r"path\columns_filter_H358_24h.csv", sep=",")
H358_72 = pd.read_csv(r"path\columns_filter_H358_72h.csv", sep=",")

# Wähle nur die numerischen Spalten aus dem DataFrame aus
numerical_columns = H358_0.select_dtypes(include=[int, float])
numerical_columns1 = H358_4.select_dtypes(include=[int, float])
numerical_columns2 = H358_24.select_dtypes(include=[int, float])
numerical_columns3 = H358_72.select_dtypes(include=[int, float])

# Liste der neuen Spaltennamen erstellen
new_column_names = ['H358_A_1'] + [f'H358_A_{i+1}' for i in range(1, len(H358_0.columns))]
new_column_names1 = ['H358_B_1'] + [f'H358_B_{i+1}' for i in range(1, len(H358_4.columns))]
new_column_names2 = ['H358_C_1'] + [f'H358_C_{i+1}' for i in range(1, len(H358_24.columns))]
new_column_names3 = ['H358_D_1'] + [f'H358_D_{i+1}' for i in range(1, len(H358_72.columns))]

# Spalten ab der zweiten Spalte umbenennen
H358_0.rename(columns=dict(zip(H358_0.columns[1:], new_column_names[0:])), inplace=True)
H358_4.rename(columns=dict(zip(H358_4.columns[1:], new_column_names[0:])), inplace=True)
H358_24.rename(columns=dict(zip(H358_24.columns[1:], new_column_names[0:])), inplace=True)
H358_72.rename(columns=dict(zip(H358_72.columns[1:], new_column_names[0:])), inplace=True)

GeneList = pd.read_csv(r"C:\Users\Sam\Desktop\Uni-Arbeit\Projekte\Drafts\D2D\Fertig\1\20230918_fiktiv1_DataNodeNames.csv")

print(GeneList)
H358_0.shape
H358_4.shape
H358_24.shape
H358_72.shape

### Rename first column
H358_0.rename(columns={'Unnamed: 0': 'gene_name'}, inplace=True)
H358_4.rename(columns={'Unnamed: 0': 'gene_name'}, inplace=True)
H358_24.rename(columns={'Unnamed: 0': 'gene_name'}, inplace=True)
H358_72.rename(columns={'Unnamed: 0': 'gene_name'}, inplace=True)

# Compare data and output only match 
new_0 = pd.merge(H358_0, GeneList, how = ("inner"),left_on =("gene_name"), right_on =( "gene_name"))
new_4 = pd.merge(H358_4, GeneList, how = ("inner"),left_on =("gene_name"), right_on =( "gene_name"))
new_24 = pd.merge(H358_24, GeneList, how = ("inner"),left_on =("gene_name"), right_on =( "gene_name"))
new_72 = pd.merge(H358_72, GeneList, how = ("inner"),left_on =("gene_name"), right_on =( "gene_name"))
  
weg = new_0.iloc[:, [0]]

new=pd.merge(new_0,new_4, on='gene_name')
new1=pd.merge(new,new_24, on='gene_name')
new2=pd.merge(new1,new_72, on='gene_name')

b_0=new_0.max(axis=1)
b_1=new_4.max(axis=1)
b_2=new_24.max(axis=1)
b_3=new_72.max(axis=1)

b_0=np.array(b_0)

b_1=np.array(b_1)
b_2=np.array(b_2)
b_3=np.array(b_3)

# Reshape b1, um es in die gleiche Shape wie a zu bringen
b1_reshaped = b_0[:, np.newaxis]
b2_reshaped = b_1[:, np.newaxis]
b3_reshaped = b_2[:, np.newaxis]
b4_reshaped = b_3[:, np.newaxis]


a=np.array(new_0)
new_data = a[:, 1:]
a1=np.array(new_4)
new_data1 = a1[:, 1:]
a2=np.array(new_24)
new_data2 = a2[:, 1:]
a3=np.array(new_72)
new_data3 = a3[:, 1:]


newdiv1 = np.divide(new_data, b1_reshaped)
newdiv2 = np.divide(new_data1, b2_reshaped)
newdiv3 = np.divide(new_data2, b3_reshaped)
newdiv4 = np.divide(new_data3, b4_reshaped)


c = pd.DataFrame(newdiv1)
c1 = pd.DataFrame(newdiv2)
c2 = pd.DataFrame(newdiv3)
c3 = pd.DataFrame(newdiv4)

c.rename(columns=dict(zip(c.columns[0:], new_column_names[0:])), inplace=True)
c1.rename(columns=dict(zip(c1.columns[0:], new_column_names1[0:])), inplace=True)
c2.rename(columns=dict(zip(c2.columns[0:], new_column_names2[0:])), inplace=True)
c3.rename(columns=dict(zip(c3.columns[0:], new_column_names3[0:])), inplace=True)

c.insert(0,'gene_name',weg)
c1.insert(0,'gene_name',weg)
c2.insert(0,'gene_name',weg)
c3.insert(0,'gene_name',weg)

# Speichern des gefilterten DataFrames in eine CSV-Datei
c.to_csv('gefilterter_dataframe_H358_0h.csv', index=False)
c1.to_csv('gefilterter_dataframe_H358_4h.csv', index=False)
c2.to_csv('gefilterter_dataframe_H358_24h.csv', index=False)
c3.to_csv('gefilterter_dataframe_H358_72h.csv', index=False)


##### Delete zeros. To solve "droupout`s
#### Create a new NumPy array without zeros.
data_without_zeros = np.where(newdiv1 != 0, newdiv1, np.nan)
data_without_zeros1 = np.where(newdiv2 != 0, newdiv2, np.nan)
data_without_zeros2 = np.where(newdiv3 != 0, newdiv3, np.nan)
data_without_zeros3 = np.where(newdiv4 != 0, newdiv4, np.nan)

###### Mean
mean=np.array(data_without_zeros.astype(float))
mean1=np.array(data_without_zeros1.astype(float))
mean2=np.array(data_without_zeros2.astype(float))
mean3=np.array(data_without_zeros3.astype(float))

##### Calculate the mean without considering NaN values.
newdiv1_mean = np.nanmean(mean, axis=1)
newdiv2_mean = np.nanmean(mean1, axis=1)
newdiv3_mean = np.nanmean(mean2, axis=1)
newdiv4_mean = np.nanmean(mean3, axis=1)
  
# STD

newdiv1_std = np.nanstd(mean, ddof=0, axis=1)
newdiv2_std = np.nanstd(mean1, ddof=0, axis=1)
newdiv3_std = np.nanstd(mean2, ddof=0, axis=1)
newdiv4_std = np.nanstd(mean3, ddof=0, axis=1)

#Test variance (can be carried out as required); threshold must be adjusted
#variance = np.var(mean, axis=1)
#threshold = 0.0001

#if np.all(variance <= threshold):
#    print("Die Varianz ist gut.")
#else:
#    print("Die Varianz ist nicht gut.")

# Transfer data to the DataFrame

end= pd.DataFrame(newdiv1)
new_column_names_a = ['H358_A_1'] + [f'H358_A_{i+1}' for i in range(1, len(end.columns))]
end.rename(columns=dict(zip(end.columns[0:], new_column_names_a[0:])), inplace=True)
end.insert(loc=len(end.columns), column=("H358_0_mean"), value=newdiv1_mean)
end.insert(loc=len(end.columns), column=("H358_0_std"), value=newdiv1_std)

end1= pd.DataFrame(newdiv2)
new_column_names_b = ['H358_B_1'] + [f'H358_B_{i+1}' for i in range(1, len(end1.columns))]
end1.rename(columns=dict(zip(end1.columns[0:], new_column_names_b[0:])), inplace=True)
end1.insert(loc=len(end1.columns), column=("H358_4_mean"), value=newdiv2_mean)
end1.insert(loc=len(end1.columns), column=("H358_4_std"), value=newdiv2_std)

end2= pd.DataFrame(newdiv3)
new_column_names_c = ['H358_C_1'] + [f'H358_C_{i+1}' for i in range(1, len(end2.columns))]
end2.rename(columns=dict(zip(end2.columns[0:], new_column_names_c[0:])), inplace=True)
end2.insert(loc=len(end2.columns), column=("H358_24_mean"), value=newdiv3_mean)
end2.insert(loc=len(end2.columns), column=("H358_24_std"), value=newdiv3_std)

end3= pd.DataFrame(newdiv4)
new_column_names_d = ['H358_D_1'] + [f'H358_D_{i+1}' for i in range(1, len(end3.columns))]
end3.rename(columns=dict(zip(end3.columns[0:], new_column_names_d[0:])), inplace=True)
end3.insert(loc=len(end3.columns), column=("H358_72_mean"), value=newdiv4_mean)
end3.insert(loc=len(end3.columns), column=("H358_772_std"), value=newdiv4_std)


end.insert(0,'gene_name',weg)
end1.insert(0,'gene_name',weg)
end2.insert(0,'gene_name',weg)
end3.insert(0,'gene_name',weg)

finish=pd.merge(end,end1, on='gene_name')
finish1=pd.merge(finish,end2, on='gene_name')
finish2=pd.merge(finish1,end3, on='gene_name')


# Export as csv.file. Intermediate storage of all information in one!
finish2.to_csv(r'path_out.csv', index=False, header=True)

####################################################################
#### all log-transformed counts
G= pd.DataFrame(newdiv1)
new_column_names_a = ['H358_A_1'] + [f'H358_A_{i+1}' for i in range(1, len(G.columns))]
G.rename(columns=dict(zip(G.columns[0:], new_column_names_a[0:])), inplace=True)
#H=G.rename(columns=lambda col: col + '_obs')

G1= pd.DataFrame(newdiv2)
new_column_names_b = ['H358_B_1'] + [f'H358_B_{i+1}' for i in range(1, len(G1.columns))]
G1.rename(columns=dict(zip(G1.columns[0:], new_column_names_b[0:])), inplace=True)


G2= pd.DataFrame(newdiv3)
new_column_names_c = ['H358_C_1'] + [f'H358_C_{i+1}' for i in range(1, len(G2.columns))]
G2.rename(columns=dict(zip(G2.columns[0:], new_column_names_c[0:])), inplace=True)


G3= pd.DataFrame(newdiv4)
new_column_names_d = ['H358_D_1'] + [f'H358_D_{i+1}' for i in range(1, len(G3.columns))]
G3.rename(columns=dict(zip(G3.columns[0:], new_column_names_d[0:])), inplace=True)

################################
#only mean and std log-transformed counts
Gene = finish.iloc[:, 0:1]

obs=(Gene+"_obs")

# Untreated counts t=0 h
obs1 = pd.DataFrame(newdiv1_mean)
obs1 = obs1.rename(columns={0: "0h"})
# Treated mean counts t=4 h
obs2= pd.DataFrame(newdiv2_mean)
obs2 = obs2.rename(columns={0: "4h"})
# Untreated counts t=24 h
obs3 = pd.DataFrame(newdiv3_mean)
obs3 = obs3.rename(columns={0: "24h"})
# Treated mean counts t=72 h
obs4= pd.DataFrame(newdiv4_mean)
obs4 = obs4.rename(columns={0: "72h"})

obsStd=(Gene+"_obs_std")


# Untreated std counts t=0 h
obsStd1 = pd.DataFrame(newdiv1_std)
obsStd1 = obsStd1.rename(columns={0: "0h"})
# Treated std counts t=4 h
obsStd2 = pd.DataFrame(newdiv2_std)
obsStd2 = obsStd2.rename(columns={0: "4h"})
# Treated std counts t=24 h
obsStd3 = pd.DataFrame(newdiv3_std)
obsStd3 = obsStd3.rename(columns={0: "24h"})
# Treated std counts t=72 h
obsStd4 = pd.DataFrame(newdiv4_std)
obsStd4 = obsStd4.rename(columns={0: "72h"})

# Concate all data and export the file as csv

data=[obs,obsStd]
data=pd.concat(data)
######
data_mean_0=[obs1,obsStd1]
data_mean_0=pd.concat(data_mean_0)

data_mean_4=[obs2,obsStd2]
data_mean_4=pd.concat(data_mean_4)

data_mean_24=[obs3,obsStd3]
data_mean_24=pd.concat(data_mean_24)

data_mean_72=[obs4,obsStd4]
data_mean_72=pd.concat(data_mean_72)
########

###############################################################

data.insert(loc=len(data.columns), column=("0"), value=data_mean_0)
data.insert(loc=len(data.columns), column=("4"), value=data_mean_4)
data.insert(loc=len(data.columns), column=("24"), value=data_mean_24)
data.insert(loc=len(data.columns), column=("72"), value=data_mean_72)

Z=data.sort_values("gene_name")
Z=Z.rename(columns={"gene_name": "time"})
ZZ=np.transpose(Z)

#######
####### Save output. This output has to be used as input in D2D --> data.csv
ZZ.to_csv(r'path_D2D_data_import.csv', index=True, header=False)



