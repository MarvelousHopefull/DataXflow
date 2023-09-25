function [ u ] = projection( u)
%Projected input u into [0,1] such that output u fulfills 0<=u<=1

u=min(max(0,u),1);
end

