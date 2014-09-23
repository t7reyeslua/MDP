clearvars;
% workigpath = mfilename('fullpath') ;

fpath  = 'C:\Users\LG\Documents\GitHub\MDP\Sandbox\Matlab\Datasets\SS';

list    = dir(fullfile(fpath, '*.txt'));
nFiles   = length(list);

for k = 1:nFiles
  file = list(k).name;

    fprintf('file: %s\n', file);

end