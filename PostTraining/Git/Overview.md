# Resources:

- [Git Rebase](https://www.youtube.com/watch?v=xN1-2p06Urc)
  > Of course git pull is (by default) just a fetch + merge, but I left out that detail for the sake of brevity. I'd like to explain this a bit better in a future video. 
  
  > Also, if you're sure that nobody else is using the branch you're pulling from, it's definitely fine to pull! So the advice from this video doesn't always apply if you're using feature branches that only one dev is working on, or if you're squashing on merge. I've just seen a lot of teams that use different branching strategies (or don't have one in place at all). But it also doesn't really hurt in that case, since doing `git pull --rebase` will just do a regular pull then. 
  
  > A common question was: Why should you abort the rebase? 
  > If you already know how to fix merge conflicts during an interactive rebase, by all means, go ahead! You don't need to abort, just fix the conflict. That's even better than aborting and merging. I just thought it would be a bit overwhelming for this video to also explain interactive rebasing and fixing merge conflicts. I do have videos planned on resolving merge conflicts and interactive rebasing, stay tuned!
  
  > One more thing people mentioned is that you can configure git pull so that it will always rebase by default. I was a bit wary of recommending this to people, since it changes the default behavior of a pretty common command, which might cause confusion (especially if you forget about it a few months later). If you want to make pull rebase the default, run `git config --global pull.rebase true`. From then on, `git pull` will rebase, and `git pull --merge` will merge. 
  > In newer versions of git, it will even ask you upon first use which pull strategy you prefer. 